package pl.dgrecki.models;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailData {
    private String to;
    private String subject;
    private String templateName;
    private String templateHtml;
    private Map<String, Object> variables;
}
