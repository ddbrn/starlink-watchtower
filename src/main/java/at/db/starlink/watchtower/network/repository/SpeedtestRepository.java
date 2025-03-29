package at.db.starlink.watchtower.network.repository;

import at.db.starlink.watchtower.network.model.Speedtest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeedtestRepository extends JpaRepository<Speedtest, Long> {
}
