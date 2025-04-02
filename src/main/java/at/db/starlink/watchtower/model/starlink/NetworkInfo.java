package at.db.starlink.watchtower.model.starlink;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Data
@NoArgsConstructor
public class NetworkInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long networkId;

    @Column(name = "domain")
    private String domain;

    @Column(name = "ipv4")
    private String ipv4;

    @ElementCollection
    @CollectionTable(name = "network_ipv6", joinColumns = @JoinColumn(name = "network_id"))
    @Column(name = "ipv6")
    private List<String> ipv6 = new ArrayList<>();

    @Column(name = "clients_ethernet")
    private int clientsEthernet;

    @Column(name = "clients_2ghz")
    private int clients2Ghz;

    @Column(name = "clients_5ghz")
    private int clients5Ghz;

    @ManyToOne
    @JoinColumn(name = "wifi_diagnostics_id")
    private WifiDiagnostics wifiDiagnostics;
}
