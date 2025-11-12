package socialMediaApp.services;

import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.Locale;
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

   public User getByEmailEntity(String email) {
       String norm = normalizeEmail(email);
       return userRepository.findByEmailIgnoreCase(norm)
               .orElseThrow(() -> new NotFoundException("User not found: email=" + norm));
   }

    public List<UserFollowingResponse> getUserFollowing(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: id=" + userId);
        }
        return userMapper.followsToFollowingResponses(
                followRepository.findAllByUser_Id(userId)
        );
    }


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
 public int add(UserAddRequest req) {
     String email = normalizeEmail(req.getEmail());
     if (email == null || email.isBlank()) {
         throw new IllegalArgumentException("EMAIL_REQUIRED");
     }


     if (userRepository.existsByEmailIgnoreCase(email)) {
         throw new AlreadyExistsException("EMAIL_ALREADY_EXISTS");
     }

     User user = userMapper.requestToUser(req);
     user.setEmail(email);

     try {
         userRepository.saveAndFlush(user);
         return user.getId();
     } catch (DataIntegrityViolationException ex) {
         if (isUniqueEmailViolation(ex)) {
             throw new AlreadyExistsException("EMAIL_ALREADY_EXISTS");
         }
         throw ex;
     }
 }

    @Transactional
    public void delete(int id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }



    private void requireUserExists(int id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: id=" + id);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isUniqueEmailViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
            String name = cve.getConstraintName();
            return name != null && name.toLowerCase().contains("email");
        }
        return false;
    }
}
