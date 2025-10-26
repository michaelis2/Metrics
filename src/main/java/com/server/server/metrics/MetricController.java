package com.server.server.metrics;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "http://localhost:8080")
public class MetricController {

    private final SystemMetricRepository repository;

    public MetricController(SystemMetricRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = {RequestMethod.OPTIONS}, path = {"/**"})
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<SystemMetric> getAllMetrics(@RequestParam(required = false) Integer days) {
        if (days == null || days <= 0) {
            return repository.getAllMetrics();
        }
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);
        return repository.getMetricsByTypeAndAfterTimestamp("CPU", fromTime); // or adjust for all metrics
    }

    @GetMapping("/cpu")
    public List<SystemMetric> getCpuMetrics(@RequestParam(required = false) Integer days) {
        return fetchMetrics("CPU", days);
    }

    @GetMapping("/memory")
    public List<SystemMetric> getMemoryMetrics(@RequestParam(required = false) Integer days) {
        return fetchMetrics("MEMORY", days);
    }

    @GetMapping("/disk")
    public List<SystemMetric> getDiskMetrics(@RequestParam(required = false) Integer days) {
        return fetchMetrics("DISK", days);
    }

    @GetMapping("/heartbeat")
    public List<SystemMetric> getHeartbeatMetrics(@RequestParam(required = false) Integer days) {
        return fetchMetrics("HEARTBEAT", days);
    }

    // ✅ Reusable logic for filtering by days
    private List<SystemMetric> fetchMetrics(String type, Integer days) {
        if (days == null || days <= 0) {
            return repository.getMetricsByType(type);
        }
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);
        return repository.getMetricsByTypeAndAfterTimestamp(type, fromTime);
    }
}
