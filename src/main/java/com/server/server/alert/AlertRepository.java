package com.server.server.alert;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface AlertRepository
        extends CrudRepository<Alert, Long> {
    public Alert findTopByOrderByTimestampDesc();

    public List<Alert> findAllByIpAddressAndMetricTypeAndActiveTrue(String var1, String var2);

    public List<Alert> findAllByOrderByTimestampDesc();
}

