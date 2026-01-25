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
import socialMediaApp.responses.ticket.TicketResponse;
import socialMediaApp.responses.ticket.TicketVerifyResponse;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    // ---- Messages / constants
    private static final String ERR_ALREADY_REGISTERED = "User already registered for this event";
    private static final String ERR_CANCELLED = "Event is cancelled";
    private static final String ERR_NOT_PUBLISHED = "Event is not published yet";
    private static final String ERR_SALES_NOT_STARTED = "Sales not started";
    private static final String ERR_SALES_ENDED = "Sales ended";
    private static final String ERR_FULL = "Event is full";
    private static final String ERR_FORBIDDEN = "FORBIDDEN";
    private static final String ERR_TICKET_NOT_FOUND = "Ticket not found";
    private static final String ERR_TICKET_OTHER_EVENT = "Ticket belongs to another event";
    private static final String ERR_TICKET_USED = "Ticket already used";
    private static final String ERR_TICKET_CANCELLED = "Ticket cancelled";
    private static final String MSG_CONSUMED = "CONSUMED";

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final PostService postService;
    private final UserService userService;
    private final EventCheckerRepository eventCheckerRepository;
    private final EventCheckerService eventCheckerService;

    private final MailService mailService;

    private final Clock clock = Clock.systemDefaultZone();

    private static final SecureRandom RNG = new SecureRandom();

    // ---------- Queries ----------

    public TicketResponse getMy(int postId, int userId) {
        Ticket t = ticketRepository.findByPost_IdAndUser_Id(postId, userId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        return ticketMapper.toResponse(t);
    }

    public List<TicketResponse> getMine(int userId) {
        var list = ticketRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
        return ticketMapper.toResponses(list);
    }

    public Map<String, Object> availability(int postId) {
        var post = postService.getById(postId);

        Integer seats = post.getCapacity(); // TOTAL
        long sold = ticketRepository.countByPost_IdAndStatusIn(
                postId,
                List.of(TicketStatus.ACTIVE, TicketStatus.USED)
        );

        Integer available = (seats == null) ? null : Math.max(0, seats - (int) sold);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("seats", seats);
        res.put("available", available);
        return res;
    }


    // ---------- Commands ----------
    @Transactional
    public TicketResponse registerPaid(int postId, int userId, String paymentIntentId, String sessionId) {
        Post post = postService.getById(postId);
        User user = userService.getById(userId);

        if (!Boolean.TRUE.equals(post.getPaid()) || post.getPrice() == null || post.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return register(postId, userId);
        }

        if (sessionId != null && ticketRepository.existsByCheckoutSessionId(sessionId)) {
            return getMy(postId, userId);
        }
        if (paymentIntentId != null && ticketRepository.existsByPaymentIntentId(paymentIntentId)) {
            return getMy(postId, userId);
        }

        if (ticketRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            return getMy(postId, userId);
        }

        assertRegistrationOpen(post);

        if (post.getCapacity() != null) {
            long sold = ticketRepository.countByPost_Id(postId);
            if (sold >= post.getCapacity()) throw new IllegalStateException("Event is full");
        }

        Ticket t = new Ticket();
        t.setPost(post);
        t.setUser(user);
        t.setCode(generateUniqueCode());
        t.setPaymentIntentId(paymentIntentId);
        t.setCheckoutSessionId(sessionId);
        t.setPaymentStatus("PAID");

        ticketRepository.save(t);

        try { mailService.sendTicketEmail(t); } catch (Exception ignored) {}

        return ticketMapper.toResponse(t);
    }


    @Transactional
    public TicketResponse register(int postId, int userId) {

        Post post = postService.getById(postId);
        User user = userService.getById(userId);

        if (Boolean.TRUE.equals(post.getPaid())
                && post.getPrice() != null
                && post.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("PAYMENT_REQUIRED");
        }

        if (ticketRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new AlreadyExistsException(ERR_ALREADY_REGISTERED);
        }

        assertRegistrationOpen(post);

        // capacity
        if (post.getCapacity() != null) {
            long sold = ticketRepository.countByPost_Id(postId);
            if (sold >= post.getCapacity()) {
                throw new IllegalStateException(ERR_FULL);
            }
        }

        Ticket t = new Ticket();
        t.setPost(post);
        t.setUser(user);
        t.setCode(generateUniqueCode());

        ticketRepository.save(t);

        try {
            mailService.sendTicketEmail(t); // потім шлемо лист
        } catch (Exception ex) {

            // log.warn("Failed to send ticket email for ticket {}", t.getId(), ex);
        }

        return ticketMapper.toResponse(t);
    }
    @Transactional(readOnly = true)
    public void sendMyTicketEmail(int postId, int actorUserId) {
        Ticket t = ticketRepository.findByPost_IdAndUser_Id(postId, actorUserId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        mailService.sendTicketEmail(t);
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
        return post.getUser().getId() == actorUserId
                || eventCheckerRepository.existsByPost_IdAndUser_Id(postId, actorUserId);
    }

    public TicketVerifyResponse validate(int postId, String rawCode, int actorUserId) {
        if (!canVerify(postId, actorUserId)) {
            return new TicketVerifyResponse(false, ERR_FORBIDDEN, null, null, null,
                    null, null, postId, null, now());
        }
        String code = normalizeCode(rawCode);
        var opt = ticketRepository.findByCode(code);
        if (opt.isEmpty()) {
            return new TicketVerifyResponse(false, "TICKET_NOT_FOUND", null, code, null,
                    null, null, postId, null, now());
        }
        var t = opt.get();
        if (t.getPost().getId() != postId) {
            return new TicketVerifyResponse(false, "TICKET_FOR_ANOTHER_EVENT", t.getId(), code, t.getStatus(),
                    t.getUser().getId(), fullNameOf(t), postId, null, now());
        }
        if (t.getStatus() != TicketStatus.ACTIVE) {
            return new TicketVerifyResponse(false, "TICKET_NOT_ACTIVE", t.getId(), code, t.getStatus(),
                    t.getUser().getId(), fullNameOf(t), postId, t.getPost().getTitle(), now());
        }
        return new TicketVerifyResponse(true, "OK", t.getId(), code, t.getStatus(),
                t.getUser().getId(), fullNameOf(t), postId, t.getPost().getTitle(), now());
    }

    @Transactional
    public TicketVerifyResponse consume(int postId, String rawCode, int actorUserId) {
        var res = validate(postId, rawCode, actorUserId);
        if (!res.isValid()) return res;

        var ticket = ticketRepository.findByCode(res.getCode()).orElseThrow();
        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        res.setStatus(TicketStatus.USED);
        res.setMessage(MSG_CONSUMED);
        return res;
    }

    @Transactional
    public TicketResponse verifyAndUse(int postId, String code, int actorUserId) {
        var post = postService.getById(postId);
        boolean allowed = post.getUser().getId() == actorUserId
                || eventCheckerService.amIChecker(postId, actorUserId);
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var ticket = ticketRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new NotFoundException(ERR_TICKET_NOT_FOUND));

        if (ticket.getPost().getId() != postId) {
            throw new IllegalStateException(ERR_TICKET_OTHER_EVENT);
        }
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException(ERR_TICKET_USED);
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException(ERR_TICKET_CANCELLED);
        }

        ticket.setStatus(TicketStatus.USED); // JPA dirty-check
        return ticketMapper.toResponse(ticket);
    }

    // ---------- Helpers ----------

    private void assertRegistrationOpen(Post post) {
        LocalDateTime now = now();
        if (post.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(ERR_CANCELLED);
        }
        if (post.getStatus() == EventStatus.DRAFT) {
            throw new IllegalStateException(ERR_NOT_PUBLISHED);
        }
        if (post.getSalesStartAt() != null && post.getSalesStartAt().isAfter(now)) {
            throw new IllegalStateException(ERR_SALES_NOT_STARTED);
        }
        if (post.getSalesEndAt() != null && post.getSalesEndAt().isBefore(now)) {
            throw new IllegalStateException(ERR_SALES_ENDED);
        }
    }

    private String normalizeCode(String rawCode) {
        return (rawCode == null ? "" : rawCode.trim()).toUpperCase();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String fullNameOf(Ticket t) {
        return t.getUser().getName() + " " + t.getUser().getLastName();
    }
}
