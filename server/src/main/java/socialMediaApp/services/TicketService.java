// src/main/java/socialMediaApp/services/TicketService.java
package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.TicketMapper;
import socialMediaApp.models.Post;
import socialMediaApp.models.Ticket;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.EventStatus;
import socialMediaApp.models.enums.TicketStatus;
import socialMediaApp.repositories.EventCheckerRepository;
import socialMediaApp.repositories.TicketRepository;
import socialMediaApp.responses.TicketVerifyResponse;
import socialMediaApp.responses.ticket.TicketResponse;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final PostService postService;
    private final UserService userService;

    private static final SecureRandom RNG = new SecureRandom();
    private final EventCheckerRepository eventCheckerRepository;
    private final EventCheckerService eventCheckerService;

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


    private String generateUniqueCode() {
        for (int i = 0; i < 5; i++) {
            byte[] bytes = new byte[8];
            RNG.nextBytes(bytes);
            String code = HexFormat.of().withUpperCase().formatHex(bytes);
            if (!ticketRepository.existsByCode(code)) return code;
        }

        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private boolean canVerify(int postId, int actorUserId) {
        var post = postService.getById(postId);
        // організатор події або призначений перевіряючий
        return post.getUser().getId() == actorUserId
                || eventCheckerRepository.existsByPost_IdAndUser_Id(postId, actorUserId);
    }

    public TicketVerifyResponse validate(int postId, String rawCode, int actorUserId) {
        if (!canVerify(postId, actorUserId)) {
            return new TicketVerifyResponse(false, "FORBIDDEN", null, null, null, null, null, postId, null, java.time.LocalDateTime.now());
        }
        String code = (rawCode == null ? "" : rawCode.trim()).toUpperCase();
        var opt = ticketRepository.findByCode(code);
        if (opt.isEmpty()) {
            return new TicketVerifyResponse(false, "TICKET_NOT_FOUND", null, code, null, null, null, postId, null, java.time.LocalDateTime.now());
        }
        var t = opt.get();
        if (t.getPost().getId() != postId) {
            return new TicketVerifyResponse(false, "TICKET_FOR_ANOTHER_EVENT", t.getId(), code, t.getStatus(),
                    t.getUser().getId(), t.getUser().getName() + " " + t.getUser().getLastName(),
                    postId, null, java.time.LocalDateTime.now());
        }
        if (t.getStatus() != TicketStatus.ACTIVE) {
            return new TicketVerifyResponse(false, "TICKET_NOT_ACTIVE", t.getId(), code, t.getStatus(),
                    t.getUser().getId(), t.getUser().getName() + " " + t.getUser().getLastName(),
                    postId, t.getPost().getTitle(), java.time.LocalDateTime.now());
        }
        return new TicketVerifyResponse(true, "OK", t.getId(), code, t.getStatus(),
                t.getUser().getId(), t.getUser().getName() + " " + t.getUser().getLastName(),
                postId, t.getPost().getTitle(), java.time.LocalDateTime.now());
    }

    @Transactional
    public TicketVerifyResponse consume(int postId, String rawCode, int actorUserId) {
        var res = validate(postId, rawCode, actorUserId);
        if (!res.isValid()) return res;


        var ticket = ticketRepository.findByCode(res.getCode()).orElseThrow();
        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);

        res.setStatus(TicketStatus.USED);
        res.setMessage("CONSUMED");
        return res;
    }
    @Transactional
    public TicketResponse verifyAndUse(int postId, String code, int actorUserId) {

        var post = postService.getById(postId);
        boolean allowed = post.getUser().getId() == actorUserId
                || eventCheckerService.amIChecker(postId, actorUserId);
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var ticket = ticketRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        if (ticket.getPost().getId() != postId) {
            throw new IllegalStateException("Ticket belongs to another event");
        }
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException("Ticket already used");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket cancelled");
        }

        ticket.setStatus(TicketStatus.USED);
        return ticketMapper.toResponse(ticket);
    }


    public Map<String, Object> availability(int postId) {
        var post = postService.getById(postId);
        long sold = ticketRepository.countByPost_Id(postId);
        Integer capacity = post.getCapacity();
        boolean full = capacity != null && sold >= capacity;
        return Map.of(
                "sold", sold,
                "capacity", capacity,
                "remaining", capacity == null ? null : Math.max(0, capacity - sold),
                "full", full
        );
    }

}
