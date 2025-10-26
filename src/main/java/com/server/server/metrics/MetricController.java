package com.server.server.metrics;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/metrics"})
@CrossOrigin(origins={"http://localhost:8080"})
public class MetricController {
    private final SystemMetricRepository repository;

    public MetricController(SystemMetricRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method={RequestMethod.OPTIONS}, path={"/**"})
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<SystemMetric> getAllMetrics() {
        return this.repository.getAllMetrics();
    }

    @GetMapping(value={"/cpu"})
    public List<SystemMetric> getCpuMetrics() {
        return this.repository.getMetricsByType("CPU");
    }

    @GetMapping(value={"/memory"})
    public List<SystemMetric> getMemoryMetrics() {
        return this.repository.getMetricsByType("MEMORY");
    }

    @GetMapping(value={"/disk"})
    public List<SystemMetric> getDiskMetrics() {
        return this.repository.getMetricsByType("DISK");
    }

    @GetMapping(value={"/heartbeat"})
    public List<SystemMetric> getHeartbeatMetrics() {
        return this.repository.getMetricsByType("HEARTBEAT");
    }
    private List<SystemMetric> fetchMetrics(String type, Integer days) {
        if (days == null || days <= 0) {
            return repository.getMetricsByType(type);
        }
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);
        return repository.getMetricsByTypeAndAfterTimestamp(type, fromTime);
    }

}

