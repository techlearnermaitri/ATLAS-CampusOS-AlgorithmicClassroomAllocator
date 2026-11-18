package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.SpaceRequest;import org.springframework.data.jpa.repository.JpaRepository;import java.time.LocalDate;import java.util.List;
public interface SpaceRequestRepository extends JpaRepository<SpaceRequest,Long>{List<SpaceRequest> findByDateOrderByCreatedAt(LocalDate date);List<SpaceRequest> findByDateAndPeriodIdOrderByCreatedAt(LocalDate date,Long periodId);}
