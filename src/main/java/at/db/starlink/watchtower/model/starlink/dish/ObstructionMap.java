package at.db.starlink.watchtower.model.starlink.dish;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObstructionMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;

    @Column(name = "num_rows")
    private int numRows;

    @Column(name = "num_cols")
    private int numCols;

    @ElementCollection
    @Column(name = "snr")
    private List<Float> snrList = new ArrayList<>();

    @Column(name = "min_elevation_deg")
    private float minElevationDeg;

    @Column(name = "max_theta_deg")
    private float maxThetaDeg;

    @Enumerated(EnumType.STRING)
    @Column(name = "map_reference_frame")
    private MapReferenceFrame mapReferenceFrame;

    @Column(name = "obstruction_image")
    byte[] obstructionImage;

    public enum MapReferenceFrame {
        FRAME_UNKNOWN, FRAME_EARTH, FRAME_UT;
    }
}
