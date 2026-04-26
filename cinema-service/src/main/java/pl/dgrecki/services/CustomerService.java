package pl.dgrecki.services;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.clients.AuthServiceClient;
import pl.dgrecki.clients.AuthUserResponse;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Customer;
import pl.dgrecki.repositories.CustomerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthServiceClient authServiceClient;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Customer getById(UUID customerId) {
        return customerRepository
                .findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer %s not found".formatted(customerId)));
    }

    @Transactional
    public Customer getOrFetchCustomer(Long userId) {
        return customerRepository.findByUserId(userId).orElseGet(() -> {
            log.info("Customer not found locally for userId {}, fetching from auth-service", userId);
            AuthUserResponse userData = authServiceClient.getUser(userId);
            Customer customer = Customer.builder()
                    .userId(userData.getId())
                    .email(userData.getEmail())
                    .firstName(userData.getFirstName())
                    .lastName(userData.getLastName())
                    .createdAt(clock.instant())
                    .build();
            return customerRepository.save(customer);
        });
    }
}
