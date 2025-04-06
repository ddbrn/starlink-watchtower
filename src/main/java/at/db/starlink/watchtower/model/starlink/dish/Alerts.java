package at.db.starlink.watchtower.model.starlink.dish;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Alerts {
    @Column(name = "dish_is_heating", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean dishIsHeating;

    @Column(name = "dish_thermal_throttle", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean dishThermalThrottle;

    @Column(name = "dish_thermal_shutdown", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean dishThermalShutdown;

    @Column(name = "power_supply_thermal_throttle", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean powerSupplyThermalThrottle;

    @Column(name = "motors_stuck", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean motorsStuck;

    @Column(name = "mast_not_near_vertical", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean mastNotNearVertical;

    @Column(name = "slow_ethernet_speeds", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean slowEthernetSpeeds;

    @Column(name = "software_install_pending", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean softwareInstallPending;

    @Column(name = "moving_too_fast_for_policy", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean movingTooFastForPolicy;

    @Column(name = "obstructed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean obstructed;
}
