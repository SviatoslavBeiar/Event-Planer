package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.models.OrganizerRequest;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.OrganizerRequestStatus;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.OrganizerRequestRepository;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.responses.admin.OrganizerRequestAdminDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerRequestAdminService {

    private final OrganizerRequestRepository organizerRequestRepository;
    private final UserRepository userRepository;

    public List<OrganizerRequestAdminDto> listPending() {
        return organizerRequestRepository.findDtosByStatus(OrganizerRequestStatus.PENDING);
    }
    @Transactional
    public void approve(long requestId) {
        OrganizerRequest req = organizerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("REQUEST_NOT_FOUND: id=" + requestId));

        if (req.getStatus() != OrganizerRequestStatus.PENDING) return;

        User user = req.getUser();
        user.setRole(Role.ORGANIZER);

        req.setStatus(OrganizerRequestStatus.APPROVED);
        req.setReviewedAt(LocalDateTime.now());

        userRepository.save(user);
        organizerRequestRepository.save(req);
    }

    @Transactional
    public void reject(long requestId, String note) {
        OrganizerRequest req = organizerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("REQUEST_NOT_FOUND: id=" + requestId));

        if (req.getStatus() != OrganizerRequestStatus.PENDING) return;

        req.setStatus(OrganizerRequestStatus.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        req.setNote(note);

        organizerRequestRepository.save(req);
    }
}
