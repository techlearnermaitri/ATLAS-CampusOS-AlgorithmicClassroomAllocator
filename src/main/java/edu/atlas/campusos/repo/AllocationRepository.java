package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AllocationRepository extends JpaRepository<Allocation,Long> { List<Allocation> findByStatus(String status); void deleteBySessionIdIn(Iterable<Long> ids); List<Allocation> findBySessionIdIn(Iterable<Long> ids); }
