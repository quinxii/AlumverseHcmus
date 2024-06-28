package hcmus.alumni.message.repostiory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.message.model.InboxModel;

/**
 * The InboxRepository interface provides methods for accessing and manipulating
 * inbox data in the database.
 */
public interface InboxRepository extends JpaRepository<InboxModel, Long> {
    /**
     * Retrieves the ID of the individual inbox between two users.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return the ID of the individual inbox
     */
    @Query(value = "select i.id as inbox_id from inbox i " +
            "join inbox_member im on i.id = im.inbox_id " +
            "where im.user_id in (:userId1, :userId2) " +
            "and i.is_group = false and i.is_delete = false and im.is_delete = false " +
            "group by i.id " +
            "having count(im.user_id) = 2;", nativeQuery = true)
    Long getIndividualInboxId(String userId1, String userId2);

    /**
     * Retrieves the latest inboxes for a given user.
     *
     * @param userId   the ID of the user
     * @param pageable the pagination information
     * @return a Page object containing the latest inboxes
     */
    @Query("SELECT i FROM InboxModel i " +
            "JOIN (SELECT m.inbox.id as inboxId, MAX(m.createAt) as latestMessageTime " +
            "FROM MessageModel m WHERE m.isDelete = false GROUP BY m.inbox.id) lm ON i.id = lm.inboxId " +
                "JOIN InboxMemberModel im on im.id.inboxId = i.id AND im.id.userId = :userId " +
            "ORDER BY lm.latestMessageTime DESC")
    Page<InboxModel> getLatestInboxes(String userId, Pageable pageable);

    /**
     * Retrieves the latest inboxes with search based on the provided user ID and query.
     * The search is performed based on the full name of the user who is in inbox of the provided user ID.
     *
     * @param userId  the ID of the user
     * @param query   the search query
     * @param pageable  the pagination information
     * @return a Page of InboxModel objects representing the latest inboxes that match the search criteria
     */
    @Query("SELECT i FROM InboxModel i " +
            "JOIN (SELECT m.inbox.id as inboxId, MAX(m.createAt) as latestMessageTime " +
            "FROM MessageModel m WHERE m.isDelete = false GROUP BY m.inbox.id) lm ON i.id = lm.inboxId " +
            "JOIN InboxMemberModel im1 on im1.id.inboxId = i.id AND im1.id.userId = :userId " +
            "JOIN (SELECT im2.id.inboxId as queryInboxId from InboxMemberModel im2 " +
                "WHERE im2.user.fullName like %:query% AND im2.id.userId != :userId) queryTable "+
                "ON queryTable.queryInboxId = im1.id.inboxId " +
            "ORDER BY lm.latestMessageTime DESC")
    Page<InboxModel> getLatestInboxesWithSearch(String userId, String query, Pageable pageable);
}
