package hcmus.alumni.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.dto.IFriendDto;
import hcmus.alumni.userservice.model.FriendId;
import hcmus.alumni.userservice.model.FriendModel;

public interface FriendRepository extends JpaRepository<FriendModel, FriendId> {
    
    @Query("SELECT f.id.userId as userId, f.id.friendId as friendId, f.createAt as createAt, f.isDelete as isDelete " +
            "FROM FriendModel f WHERE f.id.userId = :userId AND f.isDelete = false")
     Page<IFriendDto> findByUserIdAndIsDelete(String userId, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM FriendModel f WHERE f.id.userId = :userId AND f.isDelete = false")
    Long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT f FROM FriendModel f JOIN UserModel u ON f.id.friendId = u.id WHERE f.id.userId = :userId AND f.isDelete = false")
    Page<IFriendDto> findByUserIdAndFullName(@Param("userId") String userId, @Param("fullName") String fullName, Pageable pageable);
}
