package at.db.starlink.watchtower.controller;

import at.db.starlink.watchtower.model.starlink.dish.DishDiagnostics;
import at.db.starlink.watchtower.model.starlink.dish.ObstructionMap;
import at.db.starlink.watchtower.service.StarlinkService;
import com.spacex.api.device.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/api/starlink")
public class StarlinkController {

    private final StarlinkService starlinkService;
    private final DeviceGrpc.DeviceBlockingStub routerStub;
    private final DeviceGrpc.DeviceBlockingStub dishStub;

    public StarlinkController(StarlinkService starlinkService, DeviceGrpc.DeviceBlockingStub routerStub, DeviceGrpc.DeviceBlockingStub dishStub) {
        this.starlinkService = starlinkService;
        this.routerStub = routerStub;
        this.dishStub = dishStub;
    }

    @GetMapping(value = "/dish/diagnostics")
    public ResponseEntity<DishDiagnostics> getDishDiagnostics() {
        log.info("Received GET getDishDiagnostics()");
        DishDiagnostics dishDiagnostics = starlinkService.getDishDiagnostics();

        if (dishDiagnostics == null) {
            log.error("Error in getDishDiagnostics(): dish diagnostics is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishDiagnostics(): {}", dishDiagnostics);
        return ResponseEntity.ok(dishDiagnostics);
    }

    @GetMapping(value = "/dish/obstruction-map")
    public ResponseEntity<ObstructionMap> getDishObstructionMap(){
        log.info("Received GET getDishObstructionMap()");
        ObstructionMap obstructionMap = starlinkService.getDishObstructionMap();

        if (obstructionMap == null) {
            log.error("Error in getDishObstructionMap(): obstruction map is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishObstructionMap(): {}", obstructionMap);
        return ResponseEntity.ok(obstructionMap);
    }
}
