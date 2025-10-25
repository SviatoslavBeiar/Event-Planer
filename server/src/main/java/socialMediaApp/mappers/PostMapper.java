package socialMediaApp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import socialMediaApp.models.Post;
import socialMediaApp.requests.PostAddRequest;
import socialMediaApp.responses.post.PostGetResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "user.id",target = "userId")
    @Mapping(source = "user.lastName",target = "userLastName")
    @Mapping(source = "user.name",target = "userName")
    PostGetResponse postToGetResponse(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // контролер підставляє автора
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "postImages", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true) // дефолт у @PrePersist
    @Mapping(source = "description", target = "description")
    Post postAddRequestToPost(PostAddRequest postAddRequest);


    List<PostGetResponse> postsToGetResponses(List<Post> posts);
}
