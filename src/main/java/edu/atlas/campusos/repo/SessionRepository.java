package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.List;
public interface SessionRepository extends JpaRepository<ClassSession,Long> { List<ClassSession> findByDate(LocalDate date); boolean existsByClassIdAndDateAndStartTimeAndDivision(String classId, LocalDate date, java.time.LocalTime startTime, String division); }
