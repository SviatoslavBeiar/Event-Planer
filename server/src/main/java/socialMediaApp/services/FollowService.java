package socialMediaApp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.ForbiddenOperationException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.FollowMapper;
import socialMediaApp.models.Follow;
import socialMediaApp.repositories.FollowRepository;
import socialMediaApp.requests.FollowRequest;

@Service
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowMapper followMapper;
    private final UserService userService;

    public FollowService(FollowRepository followRepository, FollowMapper followMapper, UserService userService) {
        this.followRepository = followRepository;
        this.followMapper = followMapper;
        this.userService = userService;
    }

    @Transactional
    public void add(FollowRequest req) {

        if (req.getUserId() == req.getFollowingId()) {
            throw new ForbiddenOperationException("You cannot follow yourself");
        }


        userService.getById(req.getUserId());
        userService.getById(req.getFollowingId());


        boolean exists = followRepository.existsByUser_IdAndFollowing_Id(req.getUserId(), req.getFollowingId());
        if (exists) {
            throw new AlreadyExistsException("Already following this user");
        }

        Follow entity = followMapper.addRequestToFollow(req);
        followRepository.save(entity);
    }

    @Transactional
    public void delete(FollowRequest req) {

        Follow follow = followRepository
                .findByUser_IdAndFollowing_Id(req.getUserId(), req.getFollowingId())
                .orElseThrow(() -> new NotFoundException(
                        "Follow relation not found: userId=" + req.getUserId() + ", followingId=" + req.getFollowingId()
                ));

        followRepository.delete(follow);
    }
}
