package hcmus.alumni.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.model.VoteOptionPostAdviseId;
import hcmus.alumni.counsel.model.VoteOptionPostAdviseModel;

public interface VoteOptionPostAdviseRepository
        extends JpaRepository<VoteOptionPostAdviseModel, VoteOptionPostAdviseId> {
    @Transactional
    @Modifying
    @Query("UPDATE VoteOptionPostAdviseModel vopa " +
            "SET vopa.voteCount = vopa.voteCount + :count WHERE vopa.id.voteId = :voteId AND vopa.id.postAdviseId = :postId")
    int voteCountIncrement(Integer voteId, String postId, int count);
}
