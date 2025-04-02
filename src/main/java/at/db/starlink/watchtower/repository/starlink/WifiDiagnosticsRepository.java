package at.db.starlink.watchtower.repository.starlink;

import at.db.starlink.watchtower.model.starlink.WifiDiagnostics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WifiDiagnosticsRepository extends JpaRepository<WifiDiagnostics, String> {
}
