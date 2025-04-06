package at.db.starlink.watchtower.repository;

import at.db.starlink.watchtower.model.starlink.dish.DishDiagnostics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishDiagnosticsRepository extends JpaRepository<DishDiagnostics, String> {
}
