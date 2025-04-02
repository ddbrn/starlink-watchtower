package at.db.starlink.watchtower.model.starlink;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class DishDiagnostics {

    @Id
    private String id;

    @Column(name = "hardware_version")
    private String hardwareVersion;

    @Column(name = "software_version")
    private String softwareVersion; // Wird überschrieben, keine Historie

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "utc_offset_s")
    private int utcOffsetS;

    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_self_test")
    private TestResult hardwareSelfTest;

    @ElementCollection
    @CollectionTable(name = "dish_hardware_self_test_codes", joinColumns = @JoinColumn(name = "dish_diagnostics_id"))
    @Column(name = "test_result_code")
    @Enumerated(EnumType.STRING)
    private List<TestResultCode> hardwareSelfTestCodes;

    @Embedded
    private Alerts alerts;

    @Enumerated(EnumType.STRING)
    @Column(name = "disablement_code")
    private DisablementCode disablementCode;

    @Embedded
    private Location location;

    @Embedded
    private AlignmentStats alignmentStats;

    @Column(name = "stowed")
    private boolean stowed;

    public enum TestResult {
        NO_RESULT, PASSED, FAILED
    }

    public enum TestResultCode {
        GENERAL, BOOT_UP, CPU_VOLTAGE, DBF_AAP_CS, DBF_NUM_FEMS, DBF_READ_ERRORS, DBF_T_DIE_0, DBF_T_DIE_1,
        DBF_T_DIE_0_VALID, DBF_T_DIE_1_VALID, ETH_PRIME, EIRP, FEM_CUT, FUSE_AVS, GPS, IMU, PHY, SCP_ERROR,
        TEMPERATURE, VTSENS
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    public static class Alerts {
        @Column(name = "dish_is_heating")
        private boolean dishIsHeating;

        @Column(name = "dish_thermal_throttle")
        private boolean dishThermalThrottle;

        @Column(name = "dish_thermal_shutdown")
        private boolean dishThermalShutdown;

        @Column(name = "power_supply_thermal_throttle")
        private boolean powerSupplyThermalThrottle;

        @Column(name = "motors_stuck")
        private boolean motorsStuck;

        @Column(name = "mast_not_near_vertical")
        private boolean mastNotNearVertical;

        @Column(name = "slow_ethernet_speeds")
        private boolean slowEthernetSpeeds;

        @Column(name = "software_install_pending")
        private boolean softwareInstallPending;

        @Column(name = "moving_too_fast_for_policy")
        private boolean movingTooFastForPolicy;

        @Column(name = "obstructed")
        private boolean obstructed;
    }

    public enum DisablementCode {
        UNKNOWN, OKAY, NO_ACTIVE_ACCOUNT, TOO_FAR_FROM_SERVICE_ADDRESS, IN_OCEAN, BLOCKED_COUNTRY,
        DATA_OVERAGE_SANDBOX_POLICY, CELL_IS_DISABLED, ROAM_RESTRICTED, UNKNOWN_LOCATION, ACCOUNT_DISABLED,
        UNSUPPORTED_VERSION, MOVING_TOO_FAST_FOR_POLICY
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    public static class Location {
        @Column(name = "location_enabled")
        private boolean enabled;

        @Column(name = "latitude")
        private double latitude;

        @Column(name = "longitude")
        private double longitude;

        @Column(name = "altitude_meters")
        private double altitudeMeters;

        @Column(name = "uncertainty_meters_valid")
        private boolean uncertaintyMetersValid;

        @Column(name = "uncertainty_meters")
        private double uncertaintyMeters;

        @Column(name = "gps_time_s")
        private double gpsTimeS;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    public static class AlignmentStats {
        @Column(name = "boresight_azimuth_deg")
        private float boresightAzimuthDeg;

        @Column(name = "boresight_elevation_deg")
        private float boresightElevationDeg;

        @Column(name = "desired_boresight_azimuth_deg")
        private float desiredBoresightAzimuthDeg;

        @Column(name = "desired_boresight_elevation_deg")
        private float desiredBoresightElevationDeg;
    }
}
