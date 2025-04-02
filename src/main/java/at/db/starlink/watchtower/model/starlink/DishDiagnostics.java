package at.db.starlink.watchtower.model.starlink;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Entity
@Data
public class DishDiagnostics {
    @Id
    private String id;

    @Column(name = "hardware_version")
    private String hardwareVersion;

    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "utc_offset_s")
    private int utcOffsetS;

    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_self_test")
    private TestResult hardwareSelfTest;

    @Column(name = "dish_is_heating")
    private boolean dishIsHeating;

    public enum TestResult {
        NO_RESULT, PASSED, FAILED
    }
}
