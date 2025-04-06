package at.db.starlink.watchtower.service;

import at.db.starlink.watchtower.model.starlink.dish.DishDiagnostics;
import at.db.starlink.watchtower.model.starlink.dish.ObstructionMap;
import at.db.starlink.watchtower.repository.ObstructionMapRepository;
import com.spacex.api.device.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StarlinkService {
    private final ObstructionMapRepository obstructionMapRepository;

    private final DeviceGrpc.DeviceBlockingStub routerStub;
    private final DeviceGrpc.DeviceBlockingStub dishStub;

    public StarlinkService(ObstructionMapRepository obstructionMapRepository, DeviceGrpc.DeviceBlockingStub routerStub, DeviceGrpc.DeviceBlockingStub dishStub) {
        this.obstructionMapRepository = obstructionMapRepository;
        this.routerStub = routerStub;
        this.dishStub = dishStub;
    }

    public DishDiagnostics getDishDiagnostics(){
        log.debug("Start getDishDiagnostics()");

        Request request = Request.newBuilder()
                .setGetDiagnostics(GetDiagnosticsRequest.newBuilder().build())
                .build();

        DishDiagnostics dishDiagnostics = null;
        try {
            Response response = dishStub.handle(request);
            log.debug("getDishDiagnostics(): Dish response case: {}", response.getResponseCase());
            dishDiagnostics = mapToDishDiagnostics(response);
        } catch (Exception e) {
            log.error("getDishDiagnostics(): Error while trying to get dish diagnostics", e);
        }

        log.debug("End getDishDiagnostics(): {}", dishDiagnostics);
        return dishDiagnostics;
    }

    public ObstructionMap getDishObstructionMap(){
        log.debug("Start getDishObstructionMap()");

        Request request = Request.newBuilder()
                .setDishGetObstructionMap(DishGetObstructionMapRequest.newBuilder().build())
                .build();

        ObstructionMap obstructionMap = null;
        try {
            Response response = dishStub.handle(request);
            log.debug("getDishObstructionMap(): Dish response case: {}", response.getResponseCase());
            obstructionMap = mapToObstructionMap(response);
        } catch (Exception e) {
            log.error("getDishObstructionMap(): Error while trying to get dish diagnostics", e);
        }

        log.debug("End getDishObstructionMap(): {}", obstructionMap);
        return obstructionMap;
    }

    public void disposeObstructionMap(){
        log.debug("Start disposeObstructionMap()");

        ObstructionMap obstructionMap = getDishObstructionMap();
        obstructionMapRepository.save(obstructionMap);

        log.debug("End disposeObstructionMap()");
    }

    private ObstructionMap mapToObstructionMap(Response response) {
        log.debug("Start mapToObstructionMap()");

        if (response == null || response.getResponseCase() != Response.ResponseCase.DISH_GET_OBSTRUCTION_MAP) {
            return null;
        }

        DishGetObstructionMapResponse obstructionMapResponse = response.getDishGetObstructionMap();

        ObstructionMap obstructionMap = new ObstructionMap();
        obstructionMap.setTimestamp(ZonedDateTime.now());
        obstructionMap.setNumRows(obstructionMapResponse.getNumRows());
        obstructionMap.setNumCols(obstructionMapResponse.getNumCols());
        obstructionMap.setSnr(obstructionMapResponse.getSnrList());
        obstructionMap.setMinElevationDeg(obstructionMapResponse.getMinElevationDeg());
        obstructionMap.setMaxThetaDeg(obstructionMapResponse.getMaxThetaDeg());
        obstructionMap.setMapReferenceFrame(ObstructionMap.MapReferenceFrame.valueOf(obstructionMapResponse.getMapReferenceFrame().name()));


        log.debug("End mapToObstructionMap(): {}", obstructionMap);
        return obstructionMap;
    }

    private DishDiagnostics mapToDishDiagnostics(Response response) {
        log.debug("Start mapToDishDiagnostics()");
        if (response == null || response.getResponseCase() != Response.ResponseCase.DISH_GET_DIAGNOSTICS) {
            return null;
        }

        DishGetDiagnosticsResponse dishResponse = response.getDishGetDiagnostics();
        DishDiagnostics dishDiagnostics = new DishDiagnostics();
        dishDiagnostics.setId(dishResponse.getId());
        dishDiagnostics.setHardwareVersion(dishResponse.getHardwareVersion());
        dishDiagnostics.setSoftwareVersion(dishResponse.getSoftwareVersion());
        dishDiagnostics.setTimestamp(ZonedDateTime.now());
        dishDiagnostics.setUtcOffsetS(dishResponse.getUtcOffsetS());
        dishDiagnostics.setHardwareSelfTest(DishDiagnostics.TestResult.valueOf(dishResponse.getHardwareSelfTest().name()));
        dishDiagnostics.setHardwareSelfTestCodes(
                dishResponse.getHardwareSelfTestCodesList().stream()
                        .map(code -> DishDiagnostics.TestResultCode.valueOf(code.name()))
                        .collect(Collectors.toList())
        );

        // Alerts
        DishDiagnostics.Alerts alerts = new DishDiagnostics.Alerts();
        alerts.setDishIsHeating(dishResponse.getAlerts().getDishIsHeating());
        alerts.setDishThermalThrottle(dishResponse.getAlerts().getDishThermalThrottle());
        alerts.setDishThermalShutdown(dishResponse.getAlerts().getDishThermalShutdown());
        alerts.setPowerSupplyThermalThrottle(dishResponse.getAlerts().getPowerSupplyThermalThrottle());
        alerts.setMotorsStuck(dishResponse.getAlerts().getMotorsStuck());
        alerts.setMastNotNearVertical(dishResponse.getAlerts().getMastNotNearVertical());
        alerts.setSlowEthernetSpeeds(dishResponse.getAlerts().getSlowEthernetSpeeds());
        alerts.setSoftwareInstallPending(dishResponse.getAlerts().getSoftwareInstallPending());
        alerts.setMovingTooFastForPolicy(dishResponse.getAlerts().getMovingTooFastForPolicy());
        alerts.setObstructed(dishResponse.getAlerts().getObstructed());
        dishDiagnostics.setAlerts(alerts);

        // Disablement Code
        dishDiagnostics.setDisablementCode(DishDiagnostics.DisablementCode.valueOf(dishResponse.getDisablementCode().name()));

        // Location
        DishDiagnostics.Location location = new DishDiagnostics.Location();
        location.setEnabled(dishResponse.getLocation().getEnabled());
        location.setLatitude(dishResponse.getLocation().getLatitude());
        location.setLongitude(dishResponse.getLocation().getLongitude());
        location.setAltitudeMeters(dishResponse.getLocation().getAltitudeMeters());
        location.setUncertaintyMetersValid(dishResponse.getLocation().getUncertaintyMetersValid());
        location.setUncertaintyMeters(dishResponse.getLocation().getUncertaintyMeters());
        location.setGpsTimeS(dishResponse.getLocation().getGpsTimeS());
        dishDiagnostics.setLocation(location);

        // Alignment Stats
        DishDiagnostics.AlignmentStats alignmentStats = new DishDiagnostics.AlignmentStats();
        alignmentStats.setBoresightAzimuthDeg(dishResponse.getAlignmentStats().getBoresightAzimuthDeg());
        alignmentStats.setBoresightElevationDeg(dishResponse.getAlignmentStats().getBoresightElevationDeg());
        alignmentStats.setDesiredBoresightAzimuthDeg(dishResponse.getAlignmentStats().getDesiredBoresightAzimuthDeg());
        alignmentStats.setDesiredBoresightElevationDeg(dishResponse.getAlignmentStats().getDesiredBoresightElevationDeg());
        dishDiagnostics.setAlignmentStats(alignmentStats);

        // Stowed
        dishDiagnostics.setStowed(dishResponse.getStowed());

        log.debug("End mapToDishDiagnostics(): {}", dishDiagnostics);
        return dishDiagnostics;
    }
}
