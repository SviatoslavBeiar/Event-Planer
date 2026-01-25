package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.models.Post;
import socialMediaApp.models.enums.TicketStatus;
import socialMediaApp.repositories.TicketRepository;
import socialMediaApp.responses.analitics.PostAnalyticsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final PostService postService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PostAnalyticsResponse getPostTicketAnalytics(int postId, int days) {

        Post post = postService.getById(postId);
        LocalDateTime from = LocalDate.now()
                .minusDays(days - 1)
                .atStartOfDay();

        long ticketsSoldTotal = ticketRepository.countByPost_Id(postId);
        long ticketsUsedTotal =
                ticketRepository.countByPost_IdAndStatus(postId, TicketStatus.USED);

        var rawSales = ticketRepository.countDailyByPost(postId, from);
        var rawAttendance = ticketRepository.countDailyAttendance(postId, from);

        List<PostAnalyticsResponse.DailyStat> salesByDay = convert(rawSales);
        List<PostAnalyticsResponse.DailyStat> attendanceByDay = convert(rawAttendance);

        Integer capacity = post.getCapacity();
        Long remaining = capacity == null
                ? null
                : Math.max(0, (long) capacity - ticketsSoldTotal);

        Double revenue = null;
        if (post.getPrice() != null) {
            revenue = post.getPrice().doubleValue() * ticketsSoldTotal;
        }

        return new PostAnalyticsResponse(
                ticketsSoldTotal,
                ticketsUsedTotal,
                capacity,
                remaining,
                revenue,
                salesByDay,
                attendanceByDay
        );
    }


    private List<PostAnalyticsResponse.DailyStat> convert(List<Object[]> raw) {
        List<PostAnalyticsResponse.DailyStat> out = new ArrayList<>();
        for (Object[] row : raw) {
            String day;
            Object d0 = row[0];
            if (d0 instanceof java.sql.Date) {
                day = ((java.sql.Date) d0).toLocalDate().format(DF);
            } else {
                day = String.valueOf(d0);
            }
            long cnt = ((Number) row[1]).longValue();
            out.add(new PostAnalyticsResponse.DailyStat(day, cnt));
        }
        return out;
    }


}
