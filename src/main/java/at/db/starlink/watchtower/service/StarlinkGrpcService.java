package at.db.starlink.watchtower.service;

import at.db.starlink.watchtower.*;
import at.db.starlink.watchtower.model.starlink.DishDiagnostics;
import at.db.starlink.watchtower.model.starlink.NetworkInfo;
import at.db.starlink.watchtower.model.starlink.WifiDiagnostics;
import at.db.starlink.watchtower.repository.starlink.DishDiagnosticsRepository;
import at.db.starlink.watchtower.repository.starlink.WifiDiagnosticsRepository;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StarlinkGrpcService {
    private final DeviceGrpc.DeviceBlockingStub routerStub;
    private final DeviceGrpc.DeviceBlockingStub dishStub;
    private final WifiDiagnosticsRepository wifiDiagnosticsRepository;
    private final DishDiagnosticsRepository dishDiagnosticsRepository;

    @Autowired
    public StarlinkGrpcService(
            @Qualifier("routerStub") DeviceGrpc.DeviceBlockingStub routerStub,
            @Qualifier("dishStub") DeviceGrpc.DeviceBlockingStub dishStub, WifiDiagnosticsRepository wifiDiagnosticsRepository, DishDiagnosticsRepository dishDiagnosticsRepository) {
        this.routerStub = routerStub;
        this.dishStub = dishStub;
        this.wifiDiagnosticsRepository = wifiDiagnosticsRepository;
        this.dishDiagnosticsRepository = dishDiagnosticsRepository;
    }

    public WifiDiagnostics getRouterDiagnostics() {
        log.debug("Start getRouterDiagnostics()");
        Request request = Request.newBuilder()
                .setGetDiagnostics(GetDiagnosticsRequest.newBuilder().build())
                .build();

        WifiDiagnostics wifiDiagnostics = null;
        try {
            Response response = routerStub.handle(request);
            log.debug("getRouterDiagnostics(): Router response case: {}", response.getResponseCase());
            wifiDiagnostics = mapToWifiDiagnostics(response);
        } catch (StatusRuntimeException e) {
            log.error("getRouterDiagnostics(): Error while trying to get router diagnostics", e);
        }

        log.debug("Return getRouterDiagnostics(): {}", wifiDiagnostics);
        return wifiDiagnostics;
    }

    public DishDiagnostics getDishDiagnostics() {
        log.debug("Start getDishDiagnostics()");
        Request request = Request.newBuilder()
                .setGetDiagnostics(GetDiagnosticsRequest.newBuilder().build())
                .build();

        DishDiagnostics dishDiagnostics = null;
        try {
            Response response = dishStub.handle(request);
            log.debug("getDishDiagnostics(): Dish response case: {}", response.getResponseCase());
            dishDiagnostics = mapToDishDiagnostics(response);
        } catch (StatusRuntimeException e) {
            log.error("getDishDiagnostics(): Error while trying to get dish diagnostics", e);
        }

        log.debug("Return getDishDiagnostics(): {}", dishDiagnostics);
        return dishDiagnostics;
    }

    public void disposeRouterDiagnostics() {
        log.debug("Start disposeRouterDiagnostics()");
        WifiDiagnostics wifiDiagnostics = getRouterDiagnostics();

        if (wifiDiagnostics != null) {
            try {
                wifiDiagnosticsRepository.save(wifiDiagnostics);
                log.info("Persisted WiFi diagnostics for ID: {}", wifiDiagnostics.getId());
            } catch (Exception e) {
                log.error("disposeRouterDiagnostics(): Error while persisting router diagnostics", e);
            }
        } else {
            log.error("disposeRouterDiagnostics(): No valid wifi diagnostics data received");
        }

        log.debug("End disposeRouterDiagnostics()");
    }

    public void disposeDishDiagnostics() {
        log.debug("Start disposeDishDiagnostics()");
        DishDiagnostics dishDiagnostics = getDishDiagnostics();

        if (dishDiagnostics != null) {
            try {
                if (dishDiagnosticsRepository.existsById(dishDiagnostics.getId())) {
                    dishDiagnosticsRepository.deleteById(dishDiagnostics.getId());
                    log.debug("Bestehender Datensatz mit ID {} gelöscht", dishDiagnostics.getId());
                }

                dishDiagnosticsRepository.save(dishDiagnostics);
                log.info("Persisted Dish diagnostics for ID: {}", dishDiagnostics.getId());
            } catch (Exception e) {
                log.error("disposeDishDiagnostics(): Error while persisting dish diagnostics", e);
            }
        } else {
            log.error("disposeDishDiagnostics(): No valid Dish diagnostics data received");
        }

        log.debug("End disposeDishDiagnostics()");
    }

    private WifiDiagnostics mapToWifiDiagnostics(Response response) {
        if (response == null || response.getResponseCase() != Response.ResponseCase.WIFI_GET_DIAGNOSTICS) {
            return null;
        }

        WifiGetDiagnosticsResponse wifiResponse = response.getWifiGetDiagnostics();
        WifiDiagnostics wifiDiagnostics = new WifiDiagnostics();
        wifiDiagnostics.setId(wifiResponse.getId());
        wifiDiagnostics.setHardwareVersion(wifiResponse.getHardwareVersion());
        wifiDiagnostics.setSoftwareVersion(wifiResponse.getSoftwareVersion());
        wifiDiagnostics.setTimestamp(ZonedDateTime.now());

        for (WifiGetDiagnosticsResponse.Network protoNetwork : wifiResponse.getNetworksList()) {
            NetworkInfo networkInfo = new NetworkInfo();
            networkInfo.setDomain(protoNetwork.getDomain());
            networkInfo.setIpv4(protoNetwork.getIpv4());
            networkInfo.setIpv6(new ArrayList<>(protoNetwork.getIpv6List()));
            networkInfo.setClientsEthernet(protoNetwork.getClientsEthernet());
            networkInfo.setClients2Ghz(protoNetwork.getClients2Ghz());
            networkInfo.setClients5Ghz(protoNetwork.getClients5Ghz());
            networkInfo.setWifiDiagnostics(wifiDiagnostics);
            wifiDiagnostics.getNetworks().add(networkInfo);
        }

        return wifiDiagnostics;
    }

    private DishDiagnostics mapToDishDiagnostics(Response response) {
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

        return dishDiagnostics;
    }
}
