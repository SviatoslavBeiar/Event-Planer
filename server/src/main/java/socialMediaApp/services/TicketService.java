// src/main/java/socialMediaApp/services/TicketService.java
package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.TicketMapper;
import socialMediaApp.models.Post;
import socialMediaApp.models.Ticket;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.EventStatus;
import socialMediaApp.repositories.TicketRepository;
import socialMediaApp.responses.ticket.TicketResponse;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final PostService postService;
    private final UserService userService;

    private static final SecureRandom RNG = new SecureRandom();

    public TicketResponse getMy(int postId, int userId) {
        Ticket t = ticketRepository.findByPost_IdAndUser_Id(postId, userId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        return ticketMapper.toResponse(t);
    }

    public List<TicketResponse> getMine(int userId) {
        var list = ticketRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
        return ticketMapper.toResponses(list);
    }
    @Transactional
    public TicketResponse register(int postId, int userId) {
        // 1) пост і користувач існують
        Post post = postService.getById(postId);
        User user = userService.getById(userId);

        // 2) вже зареєстрований?
        if (ticketRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new AlreadyExistsException("User already registered for this event");
        }

        // 3) статус/продажі/місткість
        if (post.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Event is cancelled");
        }
        if (post.getStatus() == EventStatus.DRAFT) {
            throw new IllegalStateException("Event is not published yet");
        }
        if (post.getSalesStartAt() != null && post.getSalesStartAt().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Sales not started");
        }
        if (post.getSalesEndAt() != null && post.getSalesEndAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Sales ended");
        }
        if (post.getCapacity() != null) {
            long sold = ticketRepository.countByPost_Id(postId);
            if (sold >= post.getCapacity()) {
                throw new IllegalStateException("Event is full");
            }
        }

        // 4) створити квиток
        Ticket t = new Ticket();
        t.setPost(post);
        t.setUser(user);
        t.setCode(generateUniqueCode());

        ticketRepository.save(t);
        return ticketMapper.toResponse(t);
    }

    // короткий, але “людський” код квитка
    private String generateUniqueCode() {
        for (int i = 0; i < 5; i++) {
            byte[] bytes = new byte[8];
            RNG.nextBytes(bytes);
            String code = HexFormat.of().withUpperCase().formatHex(bytes);
            if (!ticketRepository.existsByCode(code)) return code;
        }
        // на крайній випадок
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
