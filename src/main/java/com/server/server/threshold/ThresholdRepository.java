package com.server.server.threshold;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ThresholdRepository
        extends CrudRepository<Threshold, Long> {
    public List<Threshold> findByIpAddressAndMetricType(String var1, String var2);
}
