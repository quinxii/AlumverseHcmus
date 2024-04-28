package hcmus.alumni.group.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.GroupMemberModel;
import hcmus.alumni.group.common.GroupMemberRole;
import hcmus.alumni.group.dto.IGroupMemberDto;
import hcmus.alumni.group.model.GroupUserId;

public interface GroupMemberRepository extends JpaRepository<GroupMemberModel, GroupUserId> {
	@Query("SELECT gm FROM GroupMemberModel gm WHERE gm.id.group.id = :groupId")
    Page<IGroupMemberDto> searchMembers(@Param("groupId") String groupId, Pageable pageable);
	
	@Query("SELECT gm FROM GroupMemberModel gm " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId")
    Optional<GroupMemberModel> findByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupMemberModel gm SET gm.role = :role " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId")
	int updateGroupMember(@Param("groupId") String groupId, @Param("userId") String userId, @Param("role") GroupMemberRole content);
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupMemberModel gm SET gm.isDelete = true " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId")
	int deleteGroupMember(@Param("groupId") String groupId, @Param("userId") String userId);
}
