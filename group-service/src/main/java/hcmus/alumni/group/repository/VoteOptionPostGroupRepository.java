package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.VoteOptionPostGroupId;
import hcmus.alumni.group.model.VoteOptionPostGroupModel;

public interface VoteOptionPostGroupRepository
        extends JpaRepository<VoteOptionPostGroupModel, VoteOptionPostGroupId> {
    @Query(value = "select max(id) from vote_option_post_group " +
            "where post_group_id = :postId", nativeQuery = true)
    Integer getMaxVoteId(String postId);

    @Transactional
    @Modifying
    @Query("UPDATE VoteOptionPostGroupModel vopg " +
            "SET vopg.voteCount = vopg.voteCount + :count WHERE vopg.id.voteId = :voteId AND vopg.id.postGroupId = :postId")
    int voteCountIncrement(Integer voteId, String postId, int count);
}
