package hcmus.alumni.group.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.GroupMemberModel;
import hcmus.alumni.group.common.GroupMemberRole;
import hcmus.alumni.group.dto.response.IGroupMemberDto;
import hcmus.alumni.group.model.GroupUserId;
import hcmus.alumni.group.dto.response.IUserDto;

public interface GroupMemberRepository extends JpaRepository<GroupMemberModel, GroupUserId> {
	@Query(value = "select count(*) > 0 from group_member where group_id = :groupId and user_id = :userId and role = :role and is_delete = 0", nativeQuery = true)
	Long hasGroupMemberRole(String groupId, String userId, String role);
	
	@Query(value = "select count(*) > 0 from group_member where group_id = :groupId and user_id = :userId and is_delete = 0", nativeQuery = true)
	Long isMember(String groupId, String userId);
	
	@Query("SELECT gm FROM GroupMemberModel gm " + 
			"WHERE gm.id.group.id = :groupId " + 
			"AND (:role IS NULL OR gm.role = :role)" + 
			"AND isDelete = false " + 
			"AND gm.id.user.fullName LIKE %:name%")
    Page<IGroupMemberDto> searchMembers(
    		@Param("groupId") String groupId, 
    		@Param("name") String name , 
    		@Param("role") GroupMemberRole role, 
    		Pageable pageable);
	
	@Query("SELECT gm FROM GroupMemberModel gm " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId")
    Optional<GroupMemberModel> findByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupMemberModel gm SET gm.role = :role " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId AND isDelete = false")
	int updateGroupMember(@Param("groupId") String groupId, @Param("userId") String userId, @Param("role") GroupMemberRole content);
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupMemberModel gm SET gm.isDelete = true " + 
			"WHERE gm.id.group.id = :groupId AND gm.id.user.id = :userId")
	int deleteGroupMember(@Param("groupId") String groupId, @Param("userId") String userId);
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupMemberModel gm SET gm.isDelete = true " + 
			"WHERE gm.id.group.id = :groupId")
	int deleteAllGroupMember(@Param("groupId") String groupId);
	
	@Query("SELECT DISTINCT u " +
	        "FROM UserModel u " +
	        "JOIN FriendModel f ON (f.id.user.id = :requestingUserId AND f.id.friend.id = u.id OR f.id.user.id = u.id AND f.id.friend.id = :requestingUserId) " +
	        "JOIN GroupMemberModel gm ON gm.id.user.id = u.id " +
	        "WHERE gm.id.group.id = :groupId AND gm.isDelete = false AND f.isDelete = false")
	Set<IUserDto> findJoinedFriends(@Param("groupId") String groupId, @Param("requestingUserId") String requestingUserId);
}
