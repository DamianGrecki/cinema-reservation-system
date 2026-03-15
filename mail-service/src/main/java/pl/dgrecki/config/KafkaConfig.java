package pl.dgrecki.config;

import static pl.dgrecki.utils.ExceptionUtils.getRootCauseAsString;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import pl.dgrecki.exceptions.DeserializationException;
import pl.dgrecki.exceptions.HmacVerificationException;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${kafka.topics.user-registration}")
    private String userRegistrationTopic;

    @Value("${kafka.topics.user-registration-dlq}")
    private String userRegistrationDlqTopic;

    @Value("${kafka.topics.ticket-generated}")
    private String ticketGeneratedTopic;

    @Value("${kafka.topics.ticket-generated-dlq}")
    private String ticketGeneratedDlqTopic;

    private static final int INTERVAL = 5000;
    private static final int ATTEMPTS = 3;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        DeadLetterPublishingRecoverer recoverer = getDeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler errorHandler = getDefaultErrorHandler(recoverer);

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private DeadLetterPublishingRecoverer getDeadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate) {
        Map<String, String> dlqMapping = Map.of(
                userRegistrationTopic, userRegistrationDlqTopic,
                ticketGeneratedTopic, ticketGeneratedDlqTopic);

        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
            String dlqTopic = dlqMapping.get(record.topic());
            if (dlqTopic == null) {
                throw new IllegalStateException("No DLQ mapping for topic: " + record.topic());
            }
            record.headers().add("x-error-message", getRootCauseAsString(ex).getBytes());
            return new TopicPartition(dlqTopic, record.partition());
        });
    }

    private DefaultErrorHandler getDefaultErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(INTERVAL, ATTEMPTS));

        errorHandler.addNotRetryableExceptions(DeserializationException.class, HmacVerificationException.class);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error(
                    "Processing Kafka event failed: topic={}, partition={}, offset={}, attempt={}, exception={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    deliveryAttempt,
                    ex.getMessage(),
                    ex);
        });
        return errorHandler;
    }
}
