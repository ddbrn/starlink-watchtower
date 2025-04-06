package at.db.starlink.watchtower.model.starlink.dish;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dish_status_id")
    @ToString.Exclude
    private DishStatus dishStatus;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "downlink_throughput_bps")
    private float downlinkThroughputBps;

    @Column(name = "uplink_throughput_bps")
    private float uplinkThroughputBps;

    @Column(name = "pop_ping_latency_ms")
    private float popPingLatencyMs;
}