package com.server.server.threshold;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/thresholds"})
@CrossOrigin(origins={"http://localhost:8080"})
public class ThresholdController {
    private final ThresholdRepository repository;

    @Autowired
    public ThresholdController(ThresholdRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Threshold saveThreshold(@RequestBody Threshold threshold) {
        return (Threshold)this.repository.save(threshold);
    }

    @GetMapping
    public Iterable<Threshold> getAllThresholds() {
        return this.repository.findAll();
    }
}

