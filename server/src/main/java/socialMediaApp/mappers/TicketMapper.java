// src/main/java/socialMediaApp/mappers/TicketMapper.java
package socialMediaApp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import socialMediaApp.models.Ticket;
import socialMediaApp.responses.ticket.TicketResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "post.title", target = "postTitle")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(expression = "java(ticket.getUser().getName() + \" \" + ticket.getUser().getLastName())",
             target = "userFullName")
    @Mapping(source = "status",    target = "status")
    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponses(List<Ticket> tickets);


}
