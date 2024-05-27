package hcmus.alumni.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.counsel.model.UserVotePostAdviseId;
import hcmus.alumni.counsel.model.UserVotePostAdviseModel;

public interface UserVotePostAdviseRepository extends JpaRepository<UserVotePostAdviseModel, UserVotePostAdviseId> {
    @Query(value = "select count(*) from user_vote_post_advise uvpa " +
            "join vote_option_post_advise vopa on uvpa.vote_id = vopa.id " +
            "join post_advise pa on pa.id = vopa.post_advise_id " +
            "where pa.id = :postId and uvpa.user_id = :userId", nativeQuery = true)
    Long userVoteCountByPost(String postId, String userId);
}
