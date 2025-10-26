package com.server.server;

import com.server.server.alert.Alert;
import com.server.server.alert.AlertRepository;
import com.server.server.metrics.MetricType;
import com.server.server.metrics.SystemMetric;
import com.server.server.metrics.SystemMetricRepository;
import com.server.server.threshold.Threshold;
import com.server.server.threshold.ThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ServerApplicationTests {

    @Mock
    private SystemMetricRepository metricRepository;

    @Mock
    private ThresholdRepository thresholdRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private ServerApplication serverApp;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testMapType_ValidTypes() throws Exception {
        assertEquals(MetricType.HEARTBEAT, invokeMapType(1));
        assertEquals(MetricType.MEMORY, invokeMapType(2));
        assertEquals(MetricType.CPU, invokeMapType(3));
        assertEquals(MetricType.DISK, invokeMapType(4));
    }

    private MetricType invokeMapType(int code) throws Exception {
        var method = ServerApplication.class.getDeclaredMethod("mapType", int.class);
        method.setAccessible(true);
        return (MetricType) method.invoke(serverApp, code);
    }


    @Test
    void testCheckThreshold_ExceedsThresholdCreatesAlert() throws Exception {
        Threshold threshold = new Threshold();
        threshold.setThreshold(70f);

        when(thresholdRepository.findByIpAddressAndMetricType("127.0.0.1", "CPU"))
                .thenReturn(List.of(threshold));

        var method = ServerApplication.class.getDeclaredMethod(
                "checkThreshold", String.class, MetricType.class, float.class, LocalDateTime.class);
        method.setAccessible(true);

        method.invoke(serverApp, "127.0.0.1", MetricType.CPU, 80f, LocalDateTime.now());

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testUdpMessageReceivedAndSaved() throws Exception {
        byte[] data = new byte[9];
        data[0] = ServerApplication.SERVER_VERSION;

        ByteBuffer buffer = ByteBuffer.wrap(data, 1, 8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(3);
        buffer.putFloat(65.5f);

        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), 5000);

        var method = ServerApplication.class.getDeclaredMethod("processMessage", DatagramPacket.class);
        method.setAccessible(true);
        method.invoke(serverApp, packet);

        ArgumentCaptor<SystemMetric> captor = ArgumentCaptor.forClass(SystemMetric.class);
        verify(metricRepository).save(captor.capture());

        SystemMetric savedMetric = captor.getValue();
        assertEquals(MetricType.CPU, savedMetric.getType());
        assertEquals(65.5f, savedMetric.getValue());
        assertEquals("127.0.0.1", savedMetric.getClientIp());
    }

    @Test
    void testMultipleUdpMessagesSaved() throws Exception {
        for (int i = 0; i < 3; i++) {
            byte[] data = new byte[9];
            data[0] = ServerApplication.SERVER_VERSION;

            ByteBuffer buffer = ByteBuffer.wrap(data, 1, 8);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(3);
            buffer.putFloat(50 + i * 5f);

            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), 5000);

            var method = ServerApplication.class.getDeclaredMethod("processMessage", DatagramPacket.class);
            method.setAccessible(true);
            method.invoke(serverApp, packet);
        }
        verify(metricRepository, times(3)).save(any(SystemMetric.class));
    }

    @Test
    void testUdpMessageWithExtraBytes_Ignored() throws Exception {
        byte[] data = new byte[12];
        data[0] = ServerApplication.SERVER_VERSION;

        ByteBuffer buffer = ByteBuffer.wrap(data, 1, 8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(3);
        buffer.putFloat(75f);


        data[9] = (byte) 0xFF;
        data[10] = (byte) 0xAA;
        data[11] = (byte) 0x01;

        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), 5000);

        var method = ServerApplication.class.getDeclaredMethod("processMessage", DatagramPacket.class);
        method.setAccessible(true);
        method.invoke(serverApp, packet);

        ArgumentCaptor<SystemMetric> captor = ArgumentCaptor.forClass(SystemMetric.class);
        verify(metricRepository).save(captor.capture());

        SystemMetric savedMetric = captor.getValue();
        assertEquals(MetricType.CPU, savedMetric.getType());
        assertEquals(75f, savedMetric.getValue());
    }

    @Test

    void testUdpMessageWithInvalidMetricCode_Ignored() throws Exception {
        byte[] data = new byte[9];
        ByteBuffer buffer = ByteBuffer.wrap(data, 1, data.length - 1);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(99);
        buffer.putFloat(50f);

        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), 5000);

        var method = ServerApplication.class.getDeclaredMethod("processMessage", DatagramPacket.class);
        method.setAccessible(true);
        method.invoke(serverApp, packet);

        verify(metricRepository, never()).save(any());
    }


}
