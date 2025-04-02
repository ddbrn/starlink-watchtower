package at.db.starlink.watchtower.model.starlink;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class WifiDiagnostics {

    @Id
    private String id; // Unique identifier of the WiFi router

    @Column(name = "hardware_version")
    private String hardwareVersion;

    @Column(name = "software_version")
    private String softwareVersion; // Wird überschrieben, keine Historie

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "wifiDiagnostics")
    private List<NetworkInfo> networks = new ArrayList<>();
}
