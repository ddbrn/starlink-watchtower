package at.db.starlink.watchtower.network.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Data
public class Speedtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "client_isp")
    private String clientIsp;

    @Column(name = "client_country")
    private String clientCountry;

    @Column(name = "client_latitude")
    private double clientLatitude;

    @Column(name = "client_longitude")
    private double clientLongitude;

    @Column(name = "client_isp_rating")
    private double clientIspRating;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "server_country")
    private String serverCountry;

    @Column(name = "server_country_code")
    private String serverCountryCode;

    @Column(name = "server_latitude")
    private double serverLatitude;

    @Column(name = "server_longitude")
    private double serverLongitude;

    @Column(name = "server_sponsor")
    private String serverSponsor;

    @Column(name = "server_id")
    private int serverId;

    @Column(name = "server_host")
    private String serverHost;

    @Column(name = "latency")
    private double latency;

    @Column(name = "latency_distance")
    private double latencyDistance;

    @Column(name = "download_rate_in_mbps")
    private double downloadRateInMbps;

    @Column(name = "download_bytes")
    private int downloadBytes;

    @Column(name = "download_duration_in_ms")
    private long downloadDurationInMs;

    @Column(name = "upload_rate_in_mbps")
    private double uploadRateInMbps;

    @Column(name = "upload_bytes")
    private int uploadBytes;

    @Column(name = "upload_duration_in_ms")
    private long uploadDurationInMs;
}
