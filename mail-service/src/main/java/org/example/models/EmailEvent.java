package org.example.models;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailEvent {
    private String eventType;
    private String template;
    private String to;
    private String subject;
    private Map<String, Object> data;
}
