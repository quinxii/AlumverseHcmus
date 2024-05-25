package hcmus.alumni.group.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.dto.IRequestJoinGroupDto;
import hcmus.alumni.group.model.RequestJoinGroupModel;
import hcmus.alumni.group.model.GroupUserId;

public interface RequestJoinGroupRepository extends JpaRepository<RequestJoinGroupModel, GroupUserId> {        
	@Query("SELECT rj.id.user as user, rj.id.group as group FROM RequestJoinGroupModel rj " + 
			"WHERE rj.id.group.id = :groupId " + 
			"AND rj.isDelete = false")
    Page<IRequestJoinGroupDto> searchRequestJoin(@Param("groupId") String groupId, Pageable pageable);
	
	@Query("SELECT rj FROM RequestJoinGroupModel rj " + 
			"WHERE rj.id.group.id = :groupId AND rj.id.user.id = :userId")
    Optional<RequestJoinGroupModel> findByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
	
	@Transactional
	@Modifying
	@Query("UPDATE RequestJoinGroupModel rj SET rj.isDelete = true " + 
			"WHERE rj.id.group.id = :groupId AND rj.id.user.id = :userId")
	int deleteRequestJoin(@Param("groupId") String groupId, @Param("userId") String userId);
}
