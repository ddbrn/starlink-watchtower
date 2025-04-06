package at.db.starlink.watchtower.repository;

import at.db.starlink.watchtower.model.starlink.dish.ObstructionMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObstructionMapRepository extends JpaRepository<ObstructionMap, Long> {
}
