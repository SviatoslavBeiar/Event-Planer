package socialMediaApp.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.PostAddRequest;
import socialMediaApp.responses.post.PostGetResponse;
import socialMediaApp.services.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostsController {
    private final PostService postService;
    private final UserRepository userRepository;

    public PostsController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @GetMapping("/getall")
    public ResponseEntity<List<PostGetResponse>> getAll(){
        return new ResponseEntity<>(postService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/getbyid/{id}")
    public ResponseEntity<PostGetResponse> getById(@PathVariable int id){
        return new ResponseEntity<>(postService.getResponseById(id),HttpStatus.OK);
    }

    @GetMapping("/getallbyuser/{userId}")
    public ResponseEntity<List<PostGetResponse>> getAllByUser(@PathVariable int userId){
        return new ResponseEntity<>(postService.getAllByUser(userId),HttpStatus.OK);
    }

    @GetMapping("/getbyuserfollowing/{userId}")
    public ResponseEntity<List<PostGetResponse>> getAllByUserFollowing(@PathVariable int userId){
        return new ResponseEntity<>(postService.getByUserFollowing(userId),HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Integer> add(@RequestBody PostAddRequest postAddRequest, Authentication auth){
        User me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        if (me.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_ORGANIZER_CAN_ADD");
        }

        // 🔒 ігноруємо userId з фронта — підставляємо свій
        postAddRequest.setUserId(me.getId());

        int postId = postService.add(postAddRequest);
        return new ResponseEntity<>(postId,HttpStatus.CREATED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam int id){
        postService.delete(id);
        return new ResponseEntity<>("Deleted",HttpStatus.OK);
    }

}
