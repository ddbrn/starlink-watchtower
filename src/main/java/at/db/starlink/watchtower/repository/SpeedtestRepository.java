package at.db.starlink.watchtower.repository;

import at.db.starlink.watchtower.model.Speedtest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeedtestRepository extends JpaRepository<Speedtest, Long> {
}
