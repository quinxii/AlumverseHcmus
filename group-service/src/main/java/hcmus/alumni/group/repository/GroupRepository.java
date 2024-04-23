package hcmus.alumni.group.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.group.dto.IGroupDto;
import hcmus.alumni.group.model.GroupModel;

public interface GroupRepository extends JpaRepository<GroupModel, String> {        
	@Query("SELECT g " +
	   "FROM GroupModel g " +
	   "LEFT JOIN g.status s " +
	   "WHERE (:statusId IS NULL OR s.id = :statusId) " +
	   "AND g.name LIKE %:name%")
	Page<IGroupDto> searchGroups(@Param("name") String name, @Param("statusId") Integer statusId, Pageable pageable);
	
	@Query("SELECT g " +
	   "FROM GroupModel g " +
	   "WHERE g.id = :id")
	Optional<IGroupDto> findGroupById(String id);
}
