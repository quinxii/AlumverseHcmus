package hcmus.alumni.message.repostiory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.message.model.MessageModel;

/**
 * The repository interface for managing messages.
 */
public interface MessageRepository extends JpaRepository<MessageModel, Long> {

    /**
     * Retrieves a page of messages that are not deleted by inbox ID.
     *
     * @param inboxId  The ID of the inbox.
     * @param pageable The pagination information.
     * @return A page of messages.
     */
    @Query("SELECT m FROM MessageModel m WHERE m.inbox.id = :inboxId AND m.isDelete = false")
    Page<MessageModel> getByInboxId(Long inboxId, Pageable pageable);

    /**
     * Retrieves the latest messages by a list of inbox IDs.
     *
     * @param inboxIds The list of inbox IDs.
     * @return The list of latest messages.
     */
    @Query("SELECT m1 FROM MessageModel m1 JOIN " +
            "(SELECT MAX(id) as id FROM MessageModel " +
                "WHERE inbox.id in :inboxIds AND isDelete = false GROUP BY inbox.id) m2 " +
            "ON m1.id = m2.id")
    List<MessageModel> getLatestMessagesByInboxIds(List<Long> inboxIds);
}
