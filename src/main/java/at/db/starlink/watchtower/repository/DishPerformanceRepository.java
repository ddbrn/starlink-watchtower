package at.db.starlink.watchtower.repository;

import at.db.starlink.watchtower.model.starlink.dish.DishPerformance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;

public interface DishPerformanceRepository extends JpaRepository<DishPerformance, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM DishPerformance p WHERE p.timestamp < :cutoff")
    int deleteOlderThan(ZonedDateTime cutoff);
}
