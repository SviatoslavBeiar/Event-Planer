// src/main/java/socialMediaApp/mappers/EventCheckerMapper.java
package socialMediaApp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import socialMediaApp.models.EventChecker;
import socialMediaApp.responses.event.EventCheckerResponse;


import java.util.List;

@Mapper(componentModel = "spring")
public interface EventCheckerMapper {
    @Mapping(source = "id",        target = "id")
    @Mapping(source = "post.id",   target = "postId")
    @Mapping(source = "user.id",   target = "userId")
    @Mapping(expression = "java(ec.getUser().getName() + \" \" + ec.getUser().getLastName())",
            target = "userFullName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "createdAt", target = "createdAt")
    EventCheckerResponse toResponse(EventChecker ec);

    List<EventCheckerResponse> toResponses(List<EventChecker> ecs);
}
