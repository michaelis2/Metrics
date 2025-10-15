package com.server.server.metrics;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SystemMetricRepository
        extends CrudRepository<SystemMetric, Long> {
    @Query(value="SELECT * FROM SYSTEM_METRICS WHERE TYPE = :type ORDER BY TIMESTAMP", nativeQuery=true)
    public List<SystemMetric> getMetricsByType(@Param(value="type") String var1);

    @Query(value="SELECT * FROM SYSTEM_METRICS ORDER BY TIMESTAMP", nativeQuery=true)
    public List<SystemMetric> getAllMetrics();
}
