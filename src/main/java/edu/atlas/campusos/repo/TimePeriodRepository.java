package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.TimePeriod;import org.springframework.data.jpa.repository.JpaRepository;
public interface TimePeriodRepository extends JpaRepository<TimePeriod,Long>{boolean existsByNameIgnoreCase(String name);}
