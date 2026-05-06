package pl.dgrecki.services.user;

import static pl.dgrecki.constants.ExceptionMessages.USER_NOT_FOUND_MSG;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.User;
import pl.dgrecki.models.enums.UserStatus;
import pl.dgrecki.models.responses.UserResponse;
import pl.dgrecki.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getActiveUser(Long id) {
        User user = userRepository
                .findById(id)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG));
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public void setUserStatus(UserStatus userStatus, User user) {
        user.setStatus(userStatus);
        userRepository.save(user);
    }
}
