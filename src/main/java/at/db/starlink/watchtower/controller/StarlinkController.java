package at.db.starlink.watchtower.controller;

import at.db.starlink.watchtower.model.starlink.DishDiagnostics;
import at.db.starlink.watchtower.model.starlink.WifiDiagnostics;
import at.db.starlink.watchtower.service.StarlinkGrpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/api/starlink")
public class StarlinkController {
    private final StarlinkGrpcService starlinkGrpcService;


    public StarlinkController(StarlinkGrpcService starlinkGrpcService) {
        this.starlinkGrpcService = starlinkGrpcService;
    }

    @GetMapping(value = "/diagnostics/router")
    public ResponseEntity<WifiDiagnostics> getRouterDiagnostics() {
        log.info("Received GET getRouterDiagnostics()");
        WifiDiagnostics wifiDiagnostics = starlinkGrpcService.getRouterDiagnostics();

        if (wifiDiagnostics == null){
            log.error("Error in getRouterDiagnostics(): wifi diagnostics is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getRouterDiagnostics(): {}", wifiDiagnostics);
        return ResponseEntity.ok(wifiDiagnostics);
    }

    @GetMapping(value = "/diagnostics/dish")
    public ResponseEntity<DishDiagnostics> getDishDiagnostics() {
        log.info("Received GET getDishDiagnostics()");
        DishDiagnostics dishDiagnostics = starlinkGrpcService.getDishDiagnostics();

        if (dishDiagnostics == null) {
            log.error("Error in getDishDiagnostics(): dish diagnostics is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishDiagnostics(): {}", dishDiagnostics);
        return ResponseEntity.ok(dishDiagnostics);
    }

    @PostMapping(value = "/diagnostics/router/dispose")
    @Scheduled(cron = "{starlink.router.dispose.cron}")
    public ResponseEntity<Void> disposeRouterDiagnostics() {
        log.info("Received POST disposeRouterDiagnostics()");
        starlinkGrpcService.disposeRouterDiagnostics();
        log.info("Successfully disposed router diagnostics");
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/diagnostics/dish/dispose")
    @Scheduled(cron = "starlink.dish.dispose.cron")
    public ResponseEntity<Void> disposeDishDiagnostics() {
        log.info("Received POST disposeDishDiagnostics()");
        starlinkGrpcService.disposeDishDiagnostics();
        log.info("Successfully disposed dish diagnostics");
        return ResponseEntity.ok().build();
    }
}
