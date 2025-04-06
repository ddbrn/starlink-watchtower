package at.db.starlink.watchtower.model.starlink.dish;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishStatus {

    @Id
    private String id;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "hardware_version")
    private String hardwareVersion;

    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "utc_offset_s")
    private int utcOffsetS;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "generation_number")
    private long generationNumber;

    @Column(name = "uptime_s")
    private long uptimeS;

    @Enumerated(EnumType.STRING)
    @Column(name = "disablement_code")
    private DisablementCode disablementCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_actuators")
    private HasActuators hasActuators;

    @Column(name = "gps_valid", nullable = false)
    private Boolean gpsValid;

    @Column(name = "gps_sats")
    private int gpsSats;

    @Column(name = "eth_speed_mbps")
    private int ethSpeedMbps;

    @Enumerated(EnumType.STRING)
    @Column(name = "software_update_state")
    private SoftwareUpdateState softwareUpdateState;

    @Embedded
    private DishAlignmentStats alignmentStats;

    @Embedded
    private Alerts alerts;

    @Column(name = "fraction_obstructed")
    private float fractionObstructed;

    @Column(name = "currently_obstructed", nullable = false)
    private Boolean currentlyObstructed;

    @Column(name = "is_snr_above_noise_floor", nullable = false)
    private Boolean isSnrAboveNoiseFloor;

    @OneToMany(mappedBy = "dishStatus", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DishPerformance> dishPerformanceList;

    public enum HasActuators {
        HAS_ACTUATORS_UNKNOWN, HAS_ACTUATORS_YES, HAS_ACTUATORS_NO
    }

    public enum SoftwareUpdateState {
        SOFTWARE_UPDATE_STATE_UNKNOWN, IDLE, FETCHING, PRE_CHECK, WRITING, POST_CHECK, REBOOT_REQUIRED, DISABLED, FAULTED
    }
}
