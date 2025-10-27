package socialMediaApp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.UserMapper;
import socialMediaApp.models.User;
import socialMediaApp.repositories.FollowRepository;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.UserAddRequest;
import socialMediaApp.responses.user.UserFollowingResponse;
import socialMediaApp.responses.user.UserResponse;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository, FollowRepository followRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public List<UserResponse> getAll() {
        return userMapper.usersToResponses(userRepository.findAll());
    }

    public UserResponse getResponseById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
        return userMapper.userToResponse(user);
    }

    public UserResponse getByEmail(String email) {
        // findByEmail у вас повертає User (не Optional)
        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new NotFoundException("User not found: email=" + email));
        return userMapper.userToResponse(user);
    }
    public User getByEmailEntity(String email) {
        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new NotFoundException("User not found: email=" + email));
        return user;
    }
    public List<UserFollowingResponse> getUserFollowing(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return userMapper.followsToFollowingResponses(
                followRepository.findAllByUser_Id(userId)
        );
    }

    // Якщо хочете не «світити» 404 у перевірці фолоу — можна просто повернути false;
    // я залишив явні 404 для обох користувачів.
    public boolean isFollowing(int userId, int followingId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        if (!userRepository.existsById(followingId)) {
            throw new NotFoundException("User not found: id=" + followingId);
        }
        return followRepository.existsByUser_IdAndFollowing_Id(userId, followingId);
    }

    public User getById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    @Transactional
    public void add(UserAddRequest userAddRequest) {
        // Якщо зробите existsByEmail у репозиторії — краще використати його.
        if (Optional.ofNullable(userRepository.findByEmail(userAddRequest.getEmail())).isPresent()) {
            throw new AlreadyExistsException("Email already exists: " + userAddRequest.getEmail());
        }
        User user = userMapper.requestToUser(userAddRequest);
        userRepository.save(user);
    }

    @Transactional
    public void delete(int id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }
}
