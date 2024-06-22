package hcmus.alumni.group.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.dto.response.IUserVotePostGroupDto;
import hcmus.alumni.group.model.UserVotePostGroupId;
import hcmus.alumni.group.model.UserVotePostGroupModel;

public interface UserVotePostGroupRepository extends JpaRepository<UserVotePostGroupModel, UserVotePostGroupId> {
    @Query(value = "select count(*) > 0 from user_vote_post_group where vote_id = :voteId and post_group_id = :postId and user_id = :userId", nativeQuery = true)
    Long isVoteOwner(String userId, Integer voteId, String postId);

    @Query(value = "select vote_id from user_vote_post_group " +
            "where user_id = :userId and post_group_id = :postId " +
            "order by vote_id asc", nativeQuery = true)
    Set<Integer> getVoteIdsByUserAndPost(String userId, String postId);

    @Query(value = "select post_group_id, vote_id from user_vote_post_group " +
            "where user_id = :userId and post_group_id in :postId ", nativeQuery = true)
    List<Object[]> getVoteIdsByUserAndPosts(String userId, List<String> postId);

    @Query("SELECT uvpg FROM UserVotePostGroupModel uvpg " +
            "WHERE uvpg.id.voteOptionPostGroupId.voteId = :voteId AND " +
            "uvpg.id.voteOptionPostGroupId.postGroupId = :postId ")
    Page<IUserVotePostGroupDto> getUsers(Integer voteId, String postId, Pageable pageable);

    @Query(value = "select count(*) from user_vote_post_group uvpg " +
            "where uvpg.post_group_id = :postId and uvpg.user_id = :userId", nativeQuery = true)
    Long userVoteCountByPost(String postId, String userId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user_vote_post_group uvpg " +
            "SET uvpg.vote_id = :updatedVoteId " +
            "WHERE uvpg.vote_id = :oldVoteId AND uvpg.post_group_id = :postId AND uvpg.user_id = :userId", nativeQuery = true)
    int updateVoteOption(Integer updatedVoteId, String userId, Integer oldVoteId, String postId);
}
