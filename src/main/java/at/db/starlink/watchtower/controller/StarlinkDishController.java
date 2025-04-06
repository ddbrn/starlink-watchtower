package at.db.starlink.watchtower.controller;

import at.db.starlink.watchtower.model.starlink.dish.DishDiagnostics;
import at.db.starlink.watchtower.model.starlink.dish.DishStatus;
import at.db.starlink.watchtower.model.starlink.dish.ObstructionMap;
import at.db.starlink.watchtower.service.StarlinkService;
import com.spacex.api.device.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/api/starlink/dish")
public class StarlinkDishController {

    private final StarlinkService starlinkService;
    private final DeviceGrpc.DeviceBlockingStub routerStub;
    private final DeviceGrpc.DeviceBlockingStub dishStub;

    public StarlinkDishController(StarlinkService starlinkService, DeviceGrpc.DeviceBlockingStub routerStub, DeviceGrpc.DeviceBlockingStub dishStub) {
        this.starlinkService = starlinkService;
        this.routerStub = routerStub;
        this.dishStub = dishStub;
    }

    @GetMapping(value = "/v1/getDishDiagnostics")
    public ResponseEntity<DishDiagnostics> getDishDiagnostics() {
        log.info("Received GET getDishDiagnostics()");
        DishDiagnostics dishDiagnostics = starlinkService.getDishDiagnostics();

        if (dishDiagnostics == null) {
            log.error("getDishDiagnostics(): Dish diagnostics is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishDiagnostics(): {}", dishDiagnostics);
        return ResponseEntity.ok(dishDiagnostics);
    }

    @Scheduled(cron = "${starlink.dish.diagnostics.dispose}")
    @PostMapping(value = "/v1/disposeDishDiagnostics")
    public ResponseEntity<Void> disposeDishDiagnostics(){
        log.info("Received disposeDishDiagnostics()");

        DishDiagnostics dishDiagnostics = starlinkService.getDishDiagnostics();

        if (dishDiagnostics == null){
            log.error("disposeDishDiagnostics(): Dish diagnostics is null, nothing to dispose");
            return ResponseEntity.internalServerError().build();
        }
        starlinkService.disposeDishDiagnostics(dishDiagnostics);

        log.info("Return disposeDishDiagnostics(): Ok");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/v1/getObstructionMap", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getDishObstructionMap(){
        log.info("Received GET getDishObstructionMap()");
        ObstructionMap obstructionMap = starlinkService.getDishObstructionMap();

        if (obstructionMap == null) {
            log.error("getDishObstructionMap(): Obstruction map is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishObstructionMap(): {}", obstructionMap);
        return ResponseEntity.ok(obstructionMap.getObstructionImage());
    }

    @Scheduled(cron = "${starlink.dish.obstructionMap.dispose}")
    @PostMapping(value = "/v1/disposeObstructionMap")
    public ResponseEntity<Void> disposeObstructionMap(){
        log.info("Received POST disposeObstructionMap()");
        ObstructionMap obstructionMap = starlinkService.getDishObstructionMap();

        if (obstructionMap == null){
            log.error("disposeObstructionMap(): Obstruction map is null, nothing to dispose");
            return ResponseEntity.internalServerError().build();
        }
        starlinkService.disposeObstructionMap(obstructionMap);

        log.info("Return disposeObstructionMap(): Ok");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/v1/getDishStatus")
    public ResponseEntity<DishStatus> getDishStatus(){
        log.info("Received GET getDishStatus()");
        DishStatus dishStatus = starlinkService.getDishStatus();

        if (dishStatus == null) {
            log.error("getDishStatus(): Dish status is null");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Return getDishStatus(): {}", dishStatus);
        return ResponseEntity.ok(dishStatus);
    }

    @Scheduled(cron = "${starlink.dish.status.dispose}")
    @PostMapping(value = "/v1/disposeDishStatus")
    public ResponseEntity<Void> disposeDishStatus(){
        log.info("Received POST disposeDishStatus()");
        DishStatus dishStatus = starlinkService.getDishStatus();

        if (dishStatus == null){
            log.error("disposeDishStatus(): Dish status is null, nothing to dispose");
            return ResponseEntity.internalServerError().build();
        }
        starlinkService.disposeDishStatus(dishStatus);

        log.info("Return disposeDishStatus(): Ok");
        return ResponseEntity.ok().build();
    }
}
