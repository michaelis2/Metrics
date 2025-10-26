package com.server.server.metrics;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemMetricRepository extends JpaRepository<SystemMetric, Long> {
    @Query(value="SELECT * FROM SYSTEM_METRICS WHERE TYPE = :type ORDER BY TIMESTAMP", nativeQuery=true)
    public List<SystemMetric> getMetricsByType(@Param(value="type") String var1);

    @Query(value="SELECT * FROM SYSTEM_METRICS ORDER BY TIMESTAMP", nativeQuery=true)
    public List<SystemMetric> getAllMetrics();

    @Query(value = "SELECT * FROM SYSTEM_METRICS WHERE TYPE = :type AND TIMESTAMP >= :timestamp ORDER BY TIMESTAMP", nativeQuery = true)
    List<SystemMetric> getMetricsByTypeAndAfterTimestamp(
            @Param("type") String type,
            @Param("timestamp") LocalDateTime timestamp
    );
}
