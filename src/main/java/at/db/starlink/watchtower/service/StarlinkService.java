package at.db.starlink.watchtower.service;

import at.db.starlink.watchtower.model.starlink.dish.*;
import at.db.starlink.watchtower.repository.DishDiagnosticsRepository;
import at.db.starlink.watchtower.repository.DishPerformanceRepository;
import at.db.starlink.watchtower.repository.DishStatusRepository;
import at.db.starlink.watchtower.repository.ObstructionMapRepository;
import com.spacex.api.device.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StarlinkService {
    private final ObstructionMapRepository obstructionMapRepository;
    private final DishDiagnosticsRepository dishDiagnosticsRepository;
    private final DishStatusRepository dishStatusRepository;
    private final DishPerformanceRepository dishPerformanceRepository;

    private final DeviceGrpc.DeviceBlockingStub routerStub;
    private final DeviceGrpc.DeviceBlockingStub dishStub;

    @Value("${starlink.dish.performance.cleanUp.retentionHours:24}")
    private int retentionHours;

    public StarlinkService(ObstructionMapRepository obstructionMapRepository, DishDiagnosticsRepository dishDiagnosticsRepository, DishStatusRepository dishStatusRepository, DishPerformanceRepository dishPerformanceRepository, DeviceGrpc.DeviceBlockingStub routerStub, DeviceGrpc.DeviceBlockingStub dishStub) {
        this.obstructionMapRepository = obstructionMapRepository;
        this.dishDiagnosticsRepository = dishDiagnosticsRepository;
        this.dishStatusRepository = dishStatusRepository;
        this.dishPerformanceRepository = dishPerformanceRepository;
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

    public void disposeDishDiagnostics(DishDiagnostics dishDiagnostics){
        log.debug("Start disposeDishDiagnostics(dishDiagnostics: {})", dishDiagnostics);

        if (dishDiagnostics != null){
            log.debug("disposeDishDiagnostics(): Saving dish diagnostics");
            dishDiagnosticsRepository.save(dishDiagnostics);
        }

        log.debug("End disposeDishDiagnostics()");
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

    public void disposeObstructionMap(ObstructionMap obstructionMap){
        log.debug("Start disposeObstructionMap(obstructionMap: {})", obstructionMap);

        if (obstructionMap != null){
            log.debug("disposeObstructionMap(): Saving obstruction map");
            obstructionMapRepository.save(obstructionMap);
        }

        log.debug("End disposeObstructionMap()");
    }

    public DishStatus getDishStatus(){
        log.debug("Start getDishStatus()");

        Request request = Request.newBuilder()
                .setGetStatus(GetStatusRequest.newBuilder().build())
                .build();

        DishStatus dishStatus = null;
        try {
            Response response = dishStub.handle(request);
            log.debug("getDishStatus(): Dish response case: {}", response.getResponseCase());
            dishStatus = mapToDishStatus(response);
        } catch (Exception e) {
            log.error("getDishObstructionMap(): Error while trying to get dish diagnostics", e);
        }

        log.debug("End getDishStatus(): {}", dishStatus);
        return dishStatus;
    }

    public void disposeDishStatus(DishStatus dishStatus){
        log.debug("Start disposeDishStatus()");

        if (dishStatus != null){
            Optional<DishStatus> foundDishStatus = dishStatusRepository.findById(dishStatus.getId());

            List<DishPerformance> dishPerformanceList = new ArrayList<>();
            if (foundDishStatus.isPresent()){
                dishPerformanceList = foundDishStatus.get().getDishPerformanceList();
            }
            dishPerformanceList.addAll(dishStatus.getDishPerformanceList());

            // Set performance list
            dishStatus.setDishPerformanceList(dishPerformanceList);
            dishStatusRepository.save(dishStatus);
        }

        log.debug("End disposeDishStatus()");
    }

    @Scheduled(cron = "${starlink.dish.performance.cleanUp.cron}")
    public int deleteOldDishPerformance(){
        log.debug("Start deleteOldDishPerformance()");

        ZonedDateTime cutOff = ZonedDateTime.now().minusHours(retentionHours);
        log.debug("deleteOldDishPerformance(): Starting cleanup of DishPerformance entries older than {} (retention: {} hours)", cutOff, retentionHours);

        int deleteCount = dishPerformanceRepository.deleteOlderThan(cutOff);

        log.debug("End deleteOldDishPerformance(): deleteCount: {}", deleteCount);
        return deleteCount;
    }

    private DishStatus mapToDishStatus(Response response){
        log.debug("Start mapToDishStatus()");

        if (response == null || response.getResponseCase() != Response.ResponseCase.DISH_GET_STATUS) {
            return null;
        }

        DishGetStatusResponse dishGetStatusResponse = response.getDishGetStatus();
        DishStatus dishStatus = new DishStatus();
        dishStatus.setId(dishGetStatusResponse.getDeviceInfo().getId());
        dishStatus.setTimestamp(ZonedDateTime.now());
        dishStatus.setHardwareVersion(dishGetStatusResponse.getDeviceInfo().getHardwareVersion());
        dishStatus.setSoftwareVersion(dishGetStatusResponse.getDeviceInfo().getSoftwareVersion());
        dishStatus.setUtcOffsetS(dishGetStatusResponse.getDeviceInfo().getUtcOffsetS());
        dishStatus.setCountryCode(dishGetStatusResponse.getDeviceInfo().getCountryCode());
        dishStatus.setGenerationNumber(dishGetStatusResponse.getDeviceInfo().getGenerationNumber());
        dishStatus.setUptimeS(dishGetStatusResponse.getDeviceState().getUptimeS());
        dishStatus.setDisablementCode(DisablementCode.valueOf(dishGetStatusResponse.getDisablementCode().name()));
        dishStatus.setHasActuators(DishStatus.HasActuators.valueOf(dishGetStatusResponse.getHasActuators().name()));
        dishStatus.setGpsValid(dishGetStatusResponse.getGpsStats().getGpsValid());
        dishStatus.setGpsSats(dishGetStatusResponse.getGpsStats().getGpsSats());
        dishStatus.setEthSpeedMbps(dishGetStatusResponse.getEthSpeedMbps());
        dishStatus.setSoftwareUpdateState(DishStatus.SoftwareUpdateState.valueOf(dishGetStatusResponse.getSoftwareUpdateState().name()));
        dishStatus.setFractionObstructed(dishGetStatusResponse.getObstructionStats().getFractionObstructed());
        dishStatus.setCurrentlyObstructed(dishGetStatusResponse.getObstructionStats().getCurrentlyObstructed());
        dishStatus.setIsSnrAboveNoiseFloor(dishGetStatusResponse.getIsSnrAboveNoiseFloor());

        // Alerts
        Alerts alerts = new Alerts();
        alerts.setDishIsHeating(dishGetStatusResponse.getAlerts().getIsHeating());
        alerts.setDishThermalThrottle(dishGetStatusResponse.getAlerts().getThermalThrottle());
        alerts.setDishThermalShutdown(dishGetStatusResponse.getAlerts().getThermalShutdown());
        alerts.setPowerSupplyThermalThrottle(dishGetStatusResponse.getAlerts().getPowerSupplyThermalThrottle());
        alerts.setMotorsStuck(dishGetStatusResponse.getAlerts().getMotorsStuck());
        alerts.setMastNotNearVertical(dishGetStatusResponse.getAlerts().getMastNotNearVertical());
        alerts.setSlowEthernetSpeeds(dishGetStatusResponse.getAlerts().getSlowEthernetSpeeds());
        alerts.setMovingTooFastForPolicy(dishGetStatusResponse.getAlerts().getMovingTooFastForPolicy());
        dishStatus.setAlerts(alerts);

        // Alignment stats
        DishAlignmentStats alignment = new DishAlignmentStats();
        alignment.setBoresightAzimuthDeg(dishGetStatusResponse.getAlignmentStats().getBoresightAzimuthDeg());
        alignment.setBoresightElevationDeg(dishGetStatusResponse.getAlignmentStats().getBoresightElevationDeg());
        alignment.setDesiredBoresightAzimuthDeg(dishGetStatusResponse.getAlignmentStats().getDesiredBoresightAzimuthDeg());
        alignment.setDesiredBoresightElevationDeg(dishGetStatusResponse.getAlignmentStats().getDesiredBoresightElevationDeg());
        dishStatus.setAlignmentStats(alignment);

        // Performance
        DishPerformance dishPerformance = new DishPerformance();
        dishPerformance.setDishStatus(dishStatus);
        dishPerformance.setTimestamp(ZonedDateTime.now());
        dishPerformance.setDownlinkThroughputBps(dishGetStatusResponse.getDownlinkThroughputBps());
        dishPerformance.setUplinkThroughputBps(dishGetStatusResponse.getUplinkThroughputBps());
        dishPerformance.setPopPingLatencyMs(dishGetStatusResponse.getPopPingLatencyMs());
        dishStatus.setDishPerformanceList(List.of(dishPerformance));

        log.debug("End mapToDishStatus(): {}", dishStatus);
        return dishStatus;
    }

    private byte[] getObstructionMapAsPng(int numRows, int numCols, List<Float> snrList){
        log.debug("Start getObstructionMapAsPng(numRows: {}, numCols: {}, snrList: {})", numRows, numCols, snrList);
        byte[] imageBytes = new byte[0];
        try{
            BufferedImage image = new BufferedImage(numCols, numRows, BufferedImage.TYPE_INT_ARGB);

            int centerX = numCols / 2;
            int centerY = numRows / 2;
            int radius = Math.min(centerX, centerY);

            for (int row = 0; row < numRows; row++){
                for (int col = 0; col < numCols; col++){
                    int index = row * numCols + col;
                    float snr = snrList.get(index);

                    double distance = Math.sqrt(Math.pow(col - centerX, 2) + Math.pow(row - centerY, 2));
                    int rgb;

                    if (distance > radius){
                        rgb = 0x00000000;
                    }else if (snr < 0.0){
                        rgb = 0x00000000;
                    }else{
                        float point = Math.min(snr, 1.0f);
                        int r = (int) (point * 255 + (1.0 - point) * 255);
                        int g = (int) (point * 255 + (1.0 - point) * 0);
                        int b = (int) (point * 255 + (1.0 - point) * 0);
                        int a = 255;
                        rgb = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                    image.setRGB(col, row, rgb);
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            imageBytes = byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            log.error("getObstructionMapAsPng(): Error while converting to png", e);
        }
        log.debug("End getObstructionMapAsPng(): {}", imageBytes);
        return imageBytes;
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
        obstructionMap.setSnrList(obstructionMapResponse.getSnrList());
        obstructionMap.setMinElevationDeg(obstructionMapResponse.getMinElevationDeg());
        obstructionMap.setMaxThetaDeg(obstructionMapResponse.getMaxThetaDeg());
        obstructionMap.setMapReferenceFrame(ObstructionMap.MapReferenceFrame.valueOf(obstructionMapResponse.getMapReferenceFrame().name()));
        obstructionMap.setObstructionImage(getObstructionMapAsPng(obstructionMapResponse.getNumRows(), obstructionMapResponse.getNumCols(), obstructionMapResponse.getSnrList()));

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
        Alerts alerts = new Alerts();
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
        dishDiagnostics.setDisablementCode(DisablementCode.valueOf(dishResponse.getDisablementCode().name()));

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
        DishAlignmentStats alignmentStats = new DishAlignmentStats();
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
