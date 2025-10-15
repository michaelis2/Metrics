package com.server.server.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String ipAddress;
    @Column(nullable=false)
    private String metricType;
    @Column(nullable=false)
    private float thresholdValue;
    @Column(nullable=false)
    private float actualValue;
    @Column(nullable=false)
    private String message;
    @Column(nullable=false)
    private LocalDateTime timestamp;
    private boolean active;

    public Alert() {
    }

    public Alert(String ipAddress, String metricType, float thresholdValue, float actualValue, String message, LocalDateTime timestamp) {
        this.ipAddress = ipAddress;
        this.metricType = metricType;
        this.thresholdValue = thresholdValue;
        this.actualValue = actualValue;
        this.message = message;
        this.timestamp = timestamp;
        this.active = true;
    }

    public Long getId() {
        return this.id;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMetricType() {
        return this.metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public float getThresholdValue() {
        return this.thresholdValue;
    }

    public void setThresholdValue(float thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public float getActualValue() {
        return this.actualValue;
    }

    public void setActualValue(float actualValue) {
        this.actualValue = actualValue;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toString() {
        return "Alert{id=" + this.id + ", ipAddress='" + this.ipAddress + "', metricType='" + this.metricType + "', thresholdValue=" + this.thresholdValue + ", actualValue=" + this.actualValue + ", message='" + this.message + "', timestamp=" + String.valueOf(this.timestamp) + "}";
    }
}

