package com.server.server.alert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface AlertRepository extends JpaRepository<Alert, Long> {
    public Alert findTopByOrderByTimestampDesc();

    public List<Alert> findAllByIpAddressAndMetricTypeAndActiveTrue(String var1, String var2);

    public List<Alert> findAllByOrderByTimestampDesc();

    Alert findTopByIpAddressAndMetricTypeOrderByTimestampDesc(String ipAddress, String metricType);

}

