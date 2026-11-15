package edu.atlas.campusos.repo;
import edu.atlas.campusos.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoomRepository extends JpaRepository<Room,Long> { boolean existsByNameIgnoreCase(String name); }
