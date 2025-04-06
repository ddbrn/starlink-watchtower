package at.db.starlink.watchtower.repository;

import at.db.starlink.watchtower.model.starlink.dish.DishStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishStatusRepository extends JpaRepository<DishStatus, String> {
}
