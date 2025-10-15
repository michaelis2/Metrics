package com.server.server.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="system_metrics")
public class SystemMetric {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String clientIp;
    @Enumerated(value=EnumType.STRING)
    @Column(nullable=false)
    private MetricType type;
    @Column(name="metric_value", nullable=false)
    private float value;
    @Column(nullable=false)
    private LocalDateTime timestamp;

    public SystemMetric() {
    }

    public SystemMetric(String clientIp, MetricType type, float value, LocalDateTime timestamp) {
        this.clientIp = clientIp;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return this.id;
    }

    public String getClientIp() {
        return this.clientIp;
    }

    public MetricType getType() {
        return this.type;
    }

    public float getValue() {
        return this.value;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        return "SystemMetric{id=" + this.id + ", clientIp='" + this.clientIp + "', type=" + String.valueOf(this.type) + ", value=" + this.value + ", timestamp=" + String.valueOf(this.timestamp) + "}";
    }
}

