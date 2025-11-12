package socialMediaApp.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.PostAddRequest;
import socialMediaApp.requests.PostStatusUpdateRequest;
import socialMediaApp.responses.post.PostGetResponse;
import socialMediaApp.services.PostService;
import socialMediaApp.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostsController {
    private final PostService postService;


    public PostsController(PostService postService) {
        this.postService = postService;

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
    public ResponseEntity<Integer> add(@RequestBody PostAddRequest request, Authentication auth) {
        int postId = postService.addAsOrganizer(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam int id){
        postService.delete(id);
        return new ResponseEntity<>("Deleted",HttpStatus.OK);
    }


    @PutMapping("/{postId}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<PostGetResponse> updateStatus(@PathVariable int postId,
                                                        @RequestBody PostStatusUpdateRequest req,
                                                        Authentication auth) {
        return ResponseEntity.ok(
                postService.updateStatusAsOrganizer(postId, auth.getName(), req.getStatus()));
    }

}
