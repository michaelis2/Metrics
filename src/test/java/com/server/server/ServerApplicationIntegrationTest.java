package com.server.server;

import com.server.server.alert.AlertRepository;
import com.server.server.metrics.MetricType;
import com.server.server.metrics.SystemMetricRepository;
import com.server.server.threshold.Threshold;
import com.server.server.threshold.ThresholdRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ServerApplicationIntegrationTest {

    @Autowired
    private SystemMetricRepository metricRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ThresholdRepository thresholdRepository;

    private ServerApplication server;
    @BeforeEach
    public void setup() {
        alertRepository.deleteAll();
        metricRepository.deleteAll();
        thresholdRepository.deleteAll();
        server = new ServerApplication(metricRepository, thresholdRepository, alertRepository);
    }

    @BeforeAll
    static void setEnv() {
        System.setProperty("SKIP_UDP_LOOP", "true");
    }
    @Test
    public void testProcessMessage_createsMetricAndAlert_postgres() throws Exception {
        String clientIp = "127.0.0.1";


        Threshold threshold = new Threshold();
        threshold.setIpAddress(clientIp);
        threshold.setMetricType(MetricType.CPU.name());
        threshold.setThreshold(50f);
        thresholdRepository.save(threshold);


        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) 1);
        buffer.putInt(3);
        buffer.putFloat(75f);

        byte[] data = buffer.array();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(clientIp), 5000);


        server.processMessage(packet);


        var metrics = metricRepository.findAll();
        assertEquals(1, metrics.size());
        assertEquals(MetricType.CPU, metrics.get(0).getType());
        assertEquals(75f, metrics.get(0).getValue());

        var alerts = alertRepository.findAll();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).isActive());
        assertEquals(75f, alerts.get(0).getActualValue());
        assertEquals(50f, alerts.get(0).getThresholdValue());
    }
}
