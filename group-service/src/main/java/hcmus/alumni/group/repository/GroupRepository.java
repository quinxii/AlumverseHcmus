package hcmus.alumni.group.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.dto.response.IGroupDto;
import hcmus.alumni.group.model.GroupModel;
import hcmus.alumni.group.common.Privacy;

public interface GroupRepository extends JpaRepository<GroupModel, String> {
	@Query(value = "select distinct p.name from role_permission rp " +
		"join role r on r.id = rp.role_id and r.is_delete = false " +
		"join permission p on p.id = rp.permission_id and p.is_delete = false " +
		"where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
	 List<String> getPermissions(List<String> role, String domain);
	 
	@Query(value = "select count(*) > 0 from `group` where id = :groupId and creator = :userId", nativeQuery = true)
	Long isGroupOwner(String groupId, String userId);
	
	@Query(value = "select count(*) > 0 from `group` where id = :groupId and privacy = \"PRIVATE\"", nativeQuery = true)
	Long isPrivate(String groupId);

	@Query("SELECT DISTINCT new GroupModel(g, " +
        "CASE WHEN gm IS NOT NULL THEN gm.role ELSE null END, " +
        "CASE WHEN rjg IS NOT NULL THEN true ELSE false END, " + 
        ":requestingUserId, :canDelete) " +
        "FROM GroupModel g " +
        "LEFT JOIN GroupMemberModel gm ON gm.id.group.id = g.id AND gm.id.user.id = :requestingUserId AND gm.isDelete = false " +
        "LEFT JOIN RequestJoinGroupModel rjg ON rjg.id.group.id = g.id AND rjg.id.user.id = :requestingUserId AND rjg.isDelete = false " +
        "LEFT JOIN g.status s " +
        "LEFT JOIN g.tags t " +
        "WHERE (:statusId IS NULL OR s.id = :statusId) " +
        "AND (:privacy IS NULL OR g.privacy = :privacy) " +
        "AND (:isJoined IS NULL OR (gm IS NOT NULL AND :isJoined = true) OR (gm IS NULL AND :isJoined = false)) " +
        "AND g.name LIKE %:name% " +
        "AND (:tagNames IS NULL OR t.name IN :tagNames)")
	Page<IGroupDto> searchGroups(
	    @Param("name") String name,
	    @Param("tagNames") List<String> tagNames,
	    @Param("statusId") Integer statusId,
	    @Param("privacy") Privacy privacy,
	    @Param("isJoined") Boolean isJoined,
	    @Param("requestingUserId") String requestingUserId,
	    boolean canDelete,
	    Pageable pageable);

	@Query("SELECT DISTINCT new GroupModel(g, " +
        "CASE WHEN gm IS NOT NULL THEN gm.role ELSE null END, " +
        "CASE WHEN rjg IS NOT NULL THEN true ELSE false END, " +
        ":requestingUserId, :canDelete) " +
        "FROM GroupModel g " +
        "LEFT JOIN GroupMemberModel gm ON gm.id.group.id = g.id AND gm.id.user.id = :requestingUserId AND gm.isDelete = false " +
        "LEFT JOIN RequestJoinGroupModel rjg ON rjg.id.group.id = g.id AND rjg.id.user.id = :requestingUserId AND rjg.isDelete = false " +
		"WHERE g.id = :id " +
		"AND g.status.id = 2")
	Optional<IGroupDto> findGroupById(@Param("id") String id, @Param("requestingUserId") String requestingUserId, boolean canDelete);
	
	@Query("SELECT gm.id.group FROM GroupMemberModel gm WHERE gm.id.user.id = :userId AND gm.isDelete = false")
    Page<IGroupDto> findGroupsByUserId(
        @Param("userId") String userId,
        Pageable pageable
    );
	
	@Transactional
	@Modifying
	@Query("UPDATE GroupModel g SET g.participantCount = g.participantCount + :count WHERE g.id = :id")
	int participantCountIncrement(String id, @Param("count") Integer count);
}
