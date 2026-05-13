package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getUserById(UUID id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User user = findUser(id);
        user.deactivate();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findUser(id);
        user.activate();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeUserRole(UUID id, Role role) {
        User user = findUser(id);
        user.changeRole(role);
        return UserResponse.from(userRepository.save(user));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
