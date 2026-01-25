package socialMediaApp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private PostService postService;
    @Mock private UserService userService;
    @Mock private EventCheckerRepository eventCheckerRepository;
    @Mock private EventCheckerService eventCheckerService;
    @Mock private MailService mailService;

    @InjectMocks
    private TicketService ticketService;

    private User owner;
    private User user;
    private Post postFree;
    private Post postPaid;

    @BeforeEach
    void setup() {
        owner = userWithId(10);
        user  = userWithId(20);

        postFree = basePost(1, owner);
        postFree.setPaid(false);
        postFree.setPrice(BigDecimal.ZERO);
        postFree.setCurrency("PLN");
        postFree.setStatus(EventStatus.PUBLISHED);

        postPaid = basePost(2, owner);
        postPaid.setPaid(true);
        postPaid.setPrice(new BigDecimal("15.00"));
        postPaid.setCurrency("PLN");
        postPaid.setStatus(EventStatus.PUBLISHED);
    }

    // ----------------------------------------------------------------------
    // getMy / getMine
    // ----------------------------------------------------------------------

    @Test
    void getMy_whenTicketMissing_throwsNotFound() {
        when(ticketRepository.findByPost_IdAndUser_Id(1, 20)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ticketService.getMy(1, 20));
    }

    @Test
    void getMy_whenTicketExists_returnsMappedResponse() {
        Ticket t = ticket(100, postFree, user, "ABC");
        when(ticketRepository.findByPost_IdAndUser_Id(1, 20)).thenReturn(Optional.of(t));

        TicketResponse resp = mock(TicketResponse.class);
        when(ticketMapper.toResponse(t)).thenReturn(resp);

        assertSame(resp, ticketService.getMy(1, 20));
        verify(ticketMapper).toResponse(t);
    }

    @Test
    void getMine_returnsMappedResponses() {
        Ticket t1 = ticket(1, postFree, user, "A1");
        Ticket t2 = ticket(2, postFree, user, "A2");
        when(ticketRepository.findAllByUser_IdOrderByCreatedAtDesc(20)).thenReturn(List.of(t1, t2));

        List<TicketResponse> mapped = List.of(mock(TicketResponse.class), mock(TicketResponse.class));
        when(ticketMapper.toResponses(anyList())).thenReturn(mapped);

        List<TicketResponse> out = ticketService.getMine(20);
        assertSame(mapped, out);
        verify(ticketMapper).toResponses(anyList());
    }

    // ----------------------------------------------------------------------
    // availability
    // ----------------------------------------------------------------------

    @Test
    void availability_whenCapacityNull_returnsAvailableNull() {
        Post p = basePost(3, owner);
        p.setCapacity(null);
        p.setStatus(EventStatus.PUBLISHED);

        when(postService.getById(3)).thenReturn(p);
        when(ticketRepository.countByPost_IdAndStatusIn(eq(3), anyCollection())).thenReturn(5L);

        Map<String, Object> res = ticketService.availability(3);

        assertEquals(null, res.get("seats"));
        assertEquals(null, res.get("available"));
    }

    @Test
    void availability_whenCapacitySet_returnsRemaining() {
        Post p = basePost(4, owner);
        p.setCapacity(100);
        p.setStatus(EventStatus.PUBLISHED);

        when(postService.getById(4)).thenReturn(p);
        when(ticketRepository.countByPost_IdAndStatusIn(eq(4), anyCollection())).thenReturn(7L);

        Map<String, Object> res = ticketService.availability(4);

        assertEquals(100, res.get("seats"));
        assertEquals(93, res.get("available"));
    }

    // ----------------------------------------------------------------------
    // register (FREE)
    // ----------------------------------------------------------------------

    @Test
    void register_whenEventPaid_throwsPaymentRequired() {
        when(postService.getById(2)).thenReturn(postPaid);
        when(userService.getById(20)).thenReturn(user);

        assertThrows(IllegalStateException.class, () -> ticketService.register(2, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenAlreadyRegistered_throwsAlreadyExists() {
        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenEventCancelled_throws() {
        postFree.setStatus(EventStatus.CANCELLED);

        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenEventDraft_throws() {
        postFree.setStatus(EventStatus.DRAFT);

        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenSalesNotStarted_throws() {
        postFree.setSalesStartAt(LocalDateTime.now().plusHours(2));
        postFree.setStatus(EventStatus.PUBLISHED);

        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenSalesEnded_throws() {
        postFree.setSalesEndAt(LocalDateTime.now().minusMinutes(1));
        postFree.setStatus(EventStatus.PUBLISHED);

        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_whenCapacityReached_throwsFull() {
        postFree.setCapacity(2);
        postFree.setStatus(EventStatus.PUBLISHED);

        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);
        when(ticketRepository.countByPost_Id(1)).thenReturn(2L);

        assertThrows(IllegalStateException.class, () -> ticketService.register(1, 20));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void register_ok_savesTicket_andCallsMapper_andAttemptsEmail() {
        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);
        when(ticketRepository.existsByCode(anyString())).thenReturn(false);

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(mapped);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);

        TicketResponse out = ticketService.register(1, 20);

        assertSame(mapped, out);

        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertEquals(1, saved.getPost().getId());
        assertEquals(20, saved.getUser().getId());
        assertNotNull(saved.getCode());
        assertFalse(saved.getCode().isBlank());

        verify(ticketMapper).toResponse(any(Ticket.class));
        verify(mailService).sendTicketEmail(any(Ticket.class));
    }

    // ----------------------------------------------------------------------
    // registerPaid (PAID)
    // ----------------------------------------------------------------------

    @Test
    void registerPaid_whenEventIsActuallyFree_fallsBackToRegister() {
        when(postService.getById(1)).thenReturn(postFree);
        when(userService.getById(20)).thenReturn(user);

        when(ticketRepository.existsByPost_IdAndUser_Id(1, 20)).thenReturn(false);
        when(ticketRepository.existsByCode(anyString())).thenReturn(false);

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(mapped);

        TicketResponse out = ticketService.registerPaid(1, 20, "pi_x", "cs_x");
        assertSame(mapped, out);

        verify(ticketRepository).save(any(Ticket.class)); // created by register()
    }

    @Test
    void registerPaid_whenSessionAlreadyProcessed_returnsExistingTicketViaGetMy() {
        when(postService.getById(2)).thenReturn(postPaid);
        when(userService.getById(20)).thenReturn(user);
        when(ticketRepository.existsByCheckoutSessionId("cs_123")).thenReturn(true);

        Ticket existing = ticket(777, postPaid, user, "EXIST");
        when(ticketRepository.findByPost_IdAndUser_Id(2, 20)).thenReturn(Optional.of(existing));

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(existing)).thenReturn(mapped);

        TicketResponse out = ticketService.registerPaid(2, 20, "pi_ignored", "cs_123");

        assertSame(mapped, out);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void registerPaid_whenPaymentIntentAlreadyProcessed_returnsExistingTicketViaGetMy() {
        when(postService.getById(2)).thenReturn(postPaid);
        when(userService.getById(20)).thenReturn(user);

        when(ticketRepository.existsByPaymentIntentId("pi_123")).thenReturn(true);

        Ticket existing = ticket(778, postPaid, user, "EXIST2");
        when(ticketRepository.findByPost_IdAndUser_Id(2, 20)).thenReturn(Optional.of(existing));

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(existing)).thenReturn(mapped);

        TicketResponse out = ticketService.registerPaid(2, 20, "pi_123", null);

        assertSame(mapped, out);

        verify(ticketRepository, never()).save(any());
        verify(ticketRepository, never()).countByPost_Id(anyInt());
        verify(ticketRepository, never()).existsByCheckoutSessionId(anyString());
    }



    @Test
    void registerPaid_whenAlreadyHasTicket_returnsExistingTicketViaGetMy() {
        when(postService.getById(2)).thenReturn(postPaid);
        when(userService.getById(20)).thenReturn(user);

        when(ticketRepository.existsByCheckoutSessionId(any())).thenReturn(false);
        when(ticketRepository.existsByPaymentIntentId(any())).thenReturn(false);
        when(ticketRepository.existsByPost_IdAndUser_Id(2, 20)).thenReturn(true);

        Ticket existing = ticket(800, postPaid, user, "EXIST3");
        when(ticketRepository.findByPost_IdAndUser_Id(2, 20)).thenReturn(Optional.of(existing));

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(existing)).thenReturn(mapped);

        TicketResponse out = ticketService.registerPaid(2, 20, "pi_new", "cs_new");

        assertSame(mapped, out);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void registerPaid_ok_savesPaidTicket_withIds_andCallsEmail() {
        postPaid.setCapacity(100);

        when(postService.getById(2)).thenReturn(postPaid);
        when(userService.getById(20)).thenReturn(user);

        when(ticketRepository.existsByCheckoutSessionId("cs_ok")).thenReturn(false);
        when(ticketRepository.existsByPaymentIntentId("pi_ok")).thenReturn(false);
        when(ticketRepository.existsByPost_IdAndUser_Id(2, 20)).thenReturn(false);
        when(ticketRepository.existsByCode(anyString())).thenReturn(false);

        when(ticketRepository.countByPost_Id(2)).thenReturn(0L);

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(mapped);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);

        TicketResponse out = ticketService.registerPaid(2, 20, "pi_ok", "cs_ok");

        assertSame(mapped, out);

        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertEquals("PAID", saved.getPaymentStatus());
        assertEquals("pi_ok", saved.getPaymentIntentId());
        assertEquals("cs_ok", saved.getCheckoutSessionId());
        assertNotNull(saved.getCode());

        verify(mailService).sendTicketEmail(any(Ticket.class));
    }


    // ----------------------------------------------------------------------
    // validate / consume
    // ----------------------------------------------------------------------

    @Test
    void validate_whenForbidden_returnsInvalidResponse() {
        when(postService.getById(1)).thenReturn(postFree);
        // actor is NOT owner and NOT checker
        when(eventCheckerRepository.existsByPost_IdAndUser_Id(1, 999)).thenReturn(false);

        TicketVerifyResponse res = ticketService.validate(1, "ABC", 999);

        assertFalse(res.isValid());
        assertEquals("FORBIDDEN", res.getMessage());
        verify(ticketRepository, never()).findByCode(anyString());
    }

    @Test
    void validate_whenTicketNotFound_returnsInvalidResponse() {
        when(postService.getById(1)).thenReturn(postFree);
        // actor is owner -> allowed
        TicketVerifyResponse res;

        when(ticketRepository.findByCode("ABC")).thenReturn(Optional.empty());

        res = ticketService.validate(1, " abc ", owner.getId());

        assertFalse(res.isValid());
        assertEquals("TICKET_NOT_FOUND", res.getMessage());
        assertEquals("ABC", res.getCode());
    }

    @Test
    void validate_whenTicketBelongsToAnotherEvent_returnsInvalidResponse() {
        when(postService.getById(1)).thenReturn(postFree);

        Post other = basePost(999, owner);
        other.setStatus(EventStatus.PUBLISHED);

        Ticket t = ticket(5, other, user, "XYZ");
        when(ticketRepository.findByCode("XYZ")).thenReturn(Optional.of(t));

        TicketVerifyResponse res = ticketService.validate(1, "xyz", owner.getId());

        assertFalse(res.isValid());
        assertEquals("TICKET_FOR_ANOTHER_EVENT", res.getMessage());
    }

    @Test
    void validate_whenTicketNotActive_returnsInvalidResponse() {
        when(postService.getById(1)).thenReturn(postFree);

        Ticket t = ticket(6, postFree, user, "AAA");
        t.setStatus(TicketStatus.USED);

        when(ticketRepository.findByCode("AAA")).thenReturn(Optional.of(t));

        TicketVerifyResponse res = ticketService.validate(1, "aaa", owner.getId());

        assertFalse(res.isValid());
        assertEquals("TICKET_NOT_ACTIVE", res.getMessage());
        assertEquals(TicketStatus.USED, res.getStatus());
    }

    @Test
    void validate_ok_returnsValidResponse() {
        when(postService.getById(1)).thenReturn(postFree);

        Ticket t = ticket(7, postFree, user, "OK1");
        t.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByCode("OK1")).thenReturn(Optional.of(t));

        TicketVerifyResponse res = ticketService.validate(1, " ok1 ", owner.getId());

        assertTrue(res.isValid());
        assertEquals("OK", res.getMessage());
        assertEquals(TicketStatus.ACTIVE, res.getStatus());
        assertEquals("OK1", res.getCode());
    }

    @Test
    void consume_whenValidateInvalid_doesNotSave() {
        when(postService.getById(1)).thenReturn(postFree);
        when(eventCheckerRepository.existsByPost_IdAndUser_Id(1, 999)).thenReturn(false);

        TicketVerifyResponse res = ticketService.consume(1, "ABC", 999);

        assertFalse(res.isValid());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void consume_ok_marksTicketUsed_andSetsUsedAt_andSaves() {
        when(postService.getById(1)).thenReturn(postFree);

        Ticket t = ticket(8, postFree, user, "CON1");
        t.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByCode("CON1")).thenReturn(Optional.of(t));

        TicketVerifyResponse res = ticketService.consume(1, "con1", owner.getId());

        assertTrue(res.isValid());
        assertEquals("CONSUMED", res.getMessage());
        assertEquals(TicketStatus.USED, res.getStatus());

        assertEquals(TicketStatus.USED, t.getStatus());
        assertNotNull(t.getUsedAt());

        verify(ticketRepository).save(t);
    }

    // ----------------------------------------------------------------------
    // verifyAndUse
    // ----------------------------------------------------------------------

    @Test
    void verifyAndUse_whenForbidden_throws403() {
        when(postService.getById(1)).thenReturn(postFree);
        when(eventCheckerService.amIChecker(1, 999)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.verifyAndUse(1, "AAA", 999)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void verifyAndUse_whenTicketMissing_throwsNotFound() {
        when(postService.getById(1)).thenReturn(postFree);

        when(ticketRepository.findByCode("AAA")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ticketService.verifyAndUse(1, "AAA", owner.getId()));
    }


    @Test
    void verifyAndUse_ok_setsStatusUsed_andReturnsMappedResponse() {
        when(postService.getById(1)).thenReturn(postFree);

        Ticket t = ticket(9, postFree, user, "V1");
        t.setStatus(TicketStatus.ACTIVE);

        when(ticketRepository.findByCode("V1")).thenReturn(Optional.of(t));

        TicketResponse mapped = mock(TicketResponse.class);
        when(ticketMapper.toResponse(t)).thenReturn(mapped);

        TicketResponse out = ticketService.verifyAndUse(1, "v1", owner.getId());

        assertSame(mapped, out);
        assertEquals(TicketStatus.USED, t.getStatus());
        verify(ticketMapper).toResponse(t);
    }


    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private static User userWithId(int id) {
        User u = new User();
        u.setId(id);
        u.setName("N" + id);
        u.setLastName("L" + id);
        u.setEmail("u" + id + "@mail.com");
        u.setEnabled(true);
        return u;
    }

    private static Post basePost(int id, User owner) {
        Post p = new Post();
        p.setId(id);
        p.setUser(owner);
        p.setTitle("Event " + id);
        p.setStatus(EventStatus.PUBLISHED);
        p.setPaid(false);
        p.setPrice(BigDecimal.ZERO);
        p.setCurrency("PLN");
        return p;
    }

    private static Ticket ticket(int id, Post post, User user, String code) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setPost(post);
        t.setUser(user);
        t.setCode(code);
        t.setStatus(TicketStatus.ACTIVE);
        return t;
    }
}

