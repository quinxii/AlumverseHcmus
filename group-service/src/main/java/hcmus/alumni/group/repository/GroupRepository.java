package hcmus.alumni.group.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.dto.IGroupDto;
import hcmus.alumni.group.model.GroupModel;

public interface GroupRepository extends JpaRepository<GroupModel, String> {        
	@Query("SELECT g, " +
	       "CASE WHEN gm IS NOT NULL THEN true ELSE false END AS isJoined, " +
	       "CASE WHEN rjg IS NOT NULL THEN true ELSE false END AS isRequestPending, " +
	       "(SELECT COUNT(f) FROM FriendModel f WHERE f.id.user.id = :requestingUserId AND f.id.friend.id IN " +
	       "(SELECT gm.id.user.id FROM GroupMemberModel gm WHERE gm.id.group.id = g.id AND gm.isDelete = false)) AS joinedFriendsCount " +
	       "FROM GroupModel g " +
	       "LEFT JOIN GroupMemberModel gm ON gm.id.group.id = g.id AND gm.id.user.id = :requestingUserId AND gm.isDelete = false " +
	       "LEFT JOIN RequestJoinGroupModel rjg ON rjg.id.group.id = g.id AND rjg.id.user.id = :requestingUserId AND rjg.isDelete = false " +
	       "LEFT JOIN g.status s " +
	       "WHERE (:statusId IS NULL OR s.id = :statusId) " +
	       "AND g.name LIKE %:name%")
	Page<IGroupDto> searchGroups(@Param("name") String name, @Param("statusId") Integer statusId, @Param("requestingUserId") String requestingUserId, Pageable pageable);

	@Query("SELECT g, " +
	       "CASE WHEN gm IS NOT NULL THEN true ELSE false END AS isJoined, " +
	       "CASE WHEN rjg IS NOT NULL THEN true ELSE false END AS isRequestPending, " +
	       "(SELECT COUNT(f) FROM FriendModel f WHERE f.id.user.id = :requestingUserId AND f.id.friend.id IN " +
	       "(SELECT gm.id.user.id FROM GroupMemberModel gm WHERE gm.id.group.id = g.id AND gm.isDelete = false)) AS joinedFriendsCount " +
	       "FROM GroupModel g " +
	       "LEFT JOIN GroupMemberModel gm ON gm.id.group.id = g.id AND gm.id.user.id = :requestingUserId AND gm.isDelete = false " +
	       "LEFT JOIN RequestJoinGroupModel rjg ON rjg.id.group.id = g.id AND rjg.id.user.id = :requestingUserId AND rjg.isDelete = false " +
	       "WHERE g.id = :id")
	Optional<IGroupDto> findGroupById(@Param("id") String id, @Param("requestingUserId") String requestingUserId);

	
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
