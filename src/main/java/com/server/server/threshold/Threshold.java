package com.server.server.threshold;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="thresholds")
public class Threshold {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String ipAddress;
    @Column(nullable=false)
    private String metricType;
    @Column(nullable=false)
    private float threshold;

    public Threshold() {
    }

    public Threshold(String ipAddress, String metricType, float thresholdValue) {
        this.ipAddress = ipAddress;
        this.metricType = metricType;
        this.threshold = thresholdValue;
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

    public float getThreshold() {
        return this.threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
}

