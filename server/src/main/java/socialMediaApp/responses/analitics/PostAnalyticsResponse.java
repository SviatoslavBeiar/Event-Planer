package socialMediaApp.responses.analitics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PostAnalyticsResponse {
    private long ticketsSoldTotal;
    private long ticketsUsedTotal;

    private Integer capacity;
    private Long remaining;
    private Double revenue;

    private List<DailyStat> salesByDay;
    private List<DailyStat> attendanceByDay;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private String day;
        private long count;
    }
}
