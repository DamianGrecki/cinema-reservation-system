package pl.dgrecki.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthUserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
