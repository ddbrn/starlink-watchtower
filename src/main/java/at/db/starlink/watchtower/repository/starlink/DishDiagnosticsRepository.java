package at.db.starlink.watchtower.repository.starlink;

import at.db.starlink.watchtower.model.starlink.DishDiagnostics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishDiagnosticsRepository extends JpaRepository<DishDiagnostics, String> {
}
