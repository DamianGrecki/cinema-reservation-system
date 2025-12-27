package org.example.models.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailEvent<T> {
    private OutboxEvent.EventType eventType;
    private String template;
    private String to;
    private String subject;
    private T data;
}
