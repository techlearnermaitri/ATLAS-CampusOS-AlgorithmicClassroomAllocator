package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.SpaceAssignment;import org.springframework.data.jpa.repository.JpaRepository;import java.time.LocalDate;import java.util.List;
public interface SpaceAssignmentRepository extends JpaRepository<SpaceAssignment,Long>{List<SpaceAssignment> findByDate(LocalDate date);void deleteByDate(LocalDate date);}
