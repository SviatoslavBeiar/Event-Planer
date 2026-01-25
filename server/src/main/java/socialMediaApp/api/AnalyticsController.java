package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.responses.analitics.PostAnalyticsResponse;

import socialMediaApp.services.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<PostAnalyticsResponse> getForPost(
            @PathVariable int postId,
            @RequestParam(defaultValue = "7") int days
    ) {
        var res = analyticsService.getPostTicketAnalytics(postId, days);
        return ResponseEntity.ok(res);
    }

}
