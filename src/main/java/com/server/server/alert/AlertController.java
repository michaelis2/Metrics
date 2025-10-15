package com.server.server.alert;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/alerts"})
@CrossOrigin(origins={"http://localhost:8080"})
public class AlertController {
    private final AlertRepository alertRepository;

    @Autowired
    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> getAllAlerts() {
        return this.alertRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping(value={"/latest"})
    public Alert getLatestAlert() {
        return this.alertRepository.findTopByOrderByTimestampDesc();
    }
}

