package at.db.starlink.watchtower.network.controller;

import at.db.starlink.watchtower.network.SpeedtestService;
import at.db.starlink.watchtower.network.model.Speedtest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping(value = "/api/speedtest")
public class SpeedtestController {
    private final SpeedtestService speedtestService;

    public SpeedtestController(SpeedtestService speedtestService) {
        this.speedtestService = speedtestService;
    }

    @GetMapping
    public ResponseEntity<Speedtest> getSpeedtest() {
        log.debug("Received GET getSpeedtest()");
        Speedtest speedtest = speedtestService.getSpeedtest();

        if (speedtest == null) {
            log.error("Error in getSpeedtest(): speedtest is null");
            return ResponseEntity.internalServerError().build();
        }

        log.debug("Return getSpeedtest(): {}", speedtest);
        return ResponseEntity.ok(speedtest);
    }

    @PostMapping(value ="/dispose")
    @Scheduled(cron = "${speedtest.cron}")
    public ResponseEntity<Speedtest> disposeSpeedtest() {
        log.debug("Received POST disposeSpeedtest(): {}", LocalDateTime.now());

        speedtestService.disposeSpeedtest();

        log.debug("End disposeSpeedtest()");
        return ResponseEntity.ok().build();
    }
}
