package hcmus.alumni.counsel.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.dto.response.IUserVotePostAdviseDto;
import hcmus.alumni.counsel.model.UserVotePostAdviseId;
import hcmus.alumni.counsel.model.UserVotePostAdviseModel;

public interface UserVotePostAdviseRepository extends JpaRepository<UserVotePostAdviseModel, UserVotePostAdviseId> {
    @Query(value = "select count(*) > 0 from user_vote_post_advise where vote_id = :voteId and post_advise_id = :postId and user_id = :userId", nativeQuery = true)
    Long isVoteOwner(String userId, Integer voteId, String postId);

    @Query(value = "select vote_id from user_vote_post_advise " +
            "where user_id = :userId and post_advise_id = :postId " +
            "order by vote_id asc", nativeQuery = true)
    Set<Integer> getVoteIdsByUserAndPost(String userId, String postId);

    @Query(value = "select post_advise_id, vote_id from user_vote_post_advise " +
            "where user_id = :userId and post_advise_id in :postId ", nativeQuery = true)
    List<Object[]> getVoteIdsByUserAndPosts(String userId, List<String> postId);

    @Query("SELECT uvpa FROM UserVotePostAdviseModel uvpa " +
            "WHERE uvpa.id.voteOptionPostAdviseId.voteId = :voteId AND " +
            "uvpa.id.voteOptionPostAdviseId.postAdviseId = :postId ")
    Page<IUserVotePostAdviseDto> getUsers(Integer voteId, String postId, Pageable pageable);

    @Query(value = "select count(*) from user_vote_post_advise uvpa " +
            "where uvpa.post_advise_id = :postId and uvpa.user_id = :userId", nativeQuery = true)
    Long userVoteCountByPost(String postId, String userId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user_vote_post_advise uvpa " +
            "SET uvpa.vote_id = :updatedVoteId " +
            "WHERE uvpa.vote_id = :oldVoteId AND uvpa.post_advise_id = :postId AND uvpa.user_id = :userId", nativeQuery = true)
    int updateVoteOption(Integer updatedVoteId, String userId, Integer oldVoteId, String postId);
}
