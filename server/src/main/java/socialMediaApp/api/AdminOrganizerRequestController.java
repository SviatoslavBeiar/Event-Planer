package socialMediaApp.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.responses.admin.OrganizerRequestAdminDto;
import socialMediaApp.services.OrganizerRequestAdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/organizer-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizerRequestController {

    private final OrganizerRequestAdminService service;


    @GetMapping("/pending")
    public List<OrganizerRequestAdminDto> pending() {
        return service.listPending();
    }


    @PostMapping("/{id}/approve")
    public void approve(@PathVariable long id) {
        service.approve(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable long id, @RequestBody(required = false) RejectBody body) {
        service.reject(id, body == null ? null : body.getNote());
    }

    @Data
    public static class RejectBody {
        private String note;
    }
}
