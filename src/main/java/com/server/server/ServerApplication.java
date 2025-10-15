package com.server.server;

import com.server.server.alert.Alert;
import com.server.server.alert.AlertRepository;
import com.server.server.metrics.MetricType;
import com.server.server.metrics.SystemMetric;
import com.server.server.metrics.SystemMetricRepository;
import com.server.server.threshold.Threshold;
import com.server.server.threshold.ThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    private static final int BUFFER_SIZE = 1024;
    private static final byte SERVER_VERSION = 1;

    private final SystemMetricRepository metricRepository;
    private final ThresholdRepository thresholdRepository;
    private final AlertRepository alertRepository;

    @Autowired
    public ServerApplication(SystemMetricRepository metricRepository,
                             ThresholdRepository thresholdRepository,
                             AlertRepository alertRepository) {
        this.metricRepository = metricRepository;
        this.thresholdRepository = thresholdRepository;
        this.alertRepository = alertRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Read bind IP and UDP port from environment variables
        String bindIp = System.getenv().getOrDefault("SERVER_BIND_IP", "0.0.0.0");
        int udpPort = Integer.parseInt(System.getenv().getOrDefault("SERVER_UDP_PORT", "4000"));

        System.out.println("\nServer starting with:");
        System.out.println("  Bind IP: " + bindIp);
        System.out.println("  UDP Port: " + udpPort + "\n");

        try (DatagramSocket socket = new DatagramSocket(udpPort, InetAddress.getByName(bindIp))) {
            System.out.println("Server listening on " + bindIp + ":" + udpPort);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);

                // Print sender info
                System.out.println("Received packet from " + packet.getAddress() + ":" + packet.getPort());

                // Print raw bytes
                byte[] data = packet.getData();
                int length = packet.getLength();
                System.out.print("Raw bytes: ");
                for (int i = 0; i < length; i++) {
                    System.out.print(String.format("%02X ", data[i]));
                }
                System.out.println();

                // Optional: print as string (if UTF-8 text)
                String text = new String(data, 0, length);
                System.out.println("As string: " + text);

                System.out.println("-------------------------");

                processMessage(packet);
            }
        }
    }

    private void processMessage(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        byte clientVersion = data[0];
        if (clientVersion != SERVER_VERSION) {
            System.out.println("Wrong version");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data, 1, length - 1);
        buffer.order(ByteOrder.BIG_ENDIAN);

        int typeCode = buffer.getInt();
        float value = 0;

        if (typeCode != 1 && length >= 9) {
            value = buffer.getFloat();
        }

        String clientIp = packet.getAddress().getHostAddress();
        LocalDateTime now = LocalDateTime.now();

        MetricType type;
        try {
            type = mapType(typeCode);
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown message type: " + typeCode);
            return;
        }

        switch (type) {
            case HEARTBEAT -> System.out.println("Heartbeat from " + clientIp + ":" + packet.getPort());
            case MEMORY -> System.out.println("Memory usage: " + String.format("%.2f", value) + "%");
            case CPU -> System.out.println("CPU usage: " + String.format("%.2f", value) + "%");
            case DISK -> System.out.println("Disk usage: " + String.format("%.2f", value) + "%");
        }

        SystemMetric metric = new SystemMetric(clientIp, type, value,now);
        metricRepository.save(metric);
        System.out.println("Saved metric to DB: " + metric);

        checkThreshold(clientIp, type, value, now);
    }

    private MetricType mapType(int type) {
        return switch (type) {
            case 1 -> MetricType.HEARTBEAT;
            case 2 -> MetricType.MEMORY;
            case 3 -> MetricType.CPU;
            case 4 -> MetricType.DISK;
            default -> throw new IllegalArgumentException("Unknown metric type: " + type);
        };
    }

    private void checkThreshold(String ip, MetricType type, float value, LocalDateTime now) {
        List<Threshold> thresholds = thresholdRepository.findByIpAddressAndMetricType(ip, type.name());

        for (Threshold t : thresholds) {
            if (value > t.getThreshold()) {
                String message = ip + ": " + type.name() +
                        " exceeded threshold of " + t.getThreshold() +
                        "% (value = " + value + "%)";

                Alert alert = new Alert();
                alert.setIpAddress(ip);
                alert.setMetricType(type.name());
                alert.setThresholdValue(t.getThreshold());
                alert.setActualValue(value);
                alert.setMessage(message);
                alert.setTimestamp(now);
                alert.setActive(true);

                alertRepository.save(alert);

            } else {
                List<Alert> activeAlerts = alertRepository
                        .findAllByIpAddressAndMetricTypeAndActiveTrue(ip, type.name());

                for (Alert a : activeAlerts) {
                    a.setActive(false);
                    a.setActualValue(value);
                    a.setTimestamp(now);
                    alertRepository.save(a);
                }
            }
        }
    }
}
