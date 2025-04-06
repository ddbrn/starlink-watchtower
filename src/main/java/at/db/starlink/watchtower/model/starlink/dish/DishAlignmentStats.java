package at.db.starlink.watchtower.model.starlink.dish;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishAlignmentStats {
    @Column(name = "boresight_azimuth_deg")
    private float boresightAzimuthDeg;

    @Column(name = "boresight_elevation_deg")
    private float boresightElevationDeg;

    @Column(name = "desired_boresight_azimuth_deg")
    private float desiredBoresightAzimuthDeg;

    @Column(name = "desired_boresight_elevation_deg")
    private float desiredBoresightElevationDeg;
}