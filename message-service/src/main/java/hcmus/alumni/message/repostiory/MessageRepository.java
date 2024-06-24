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

    /**
     * Retrieves a list of distinct permission names based on the user's roles and a
     * specified domain.
     *
     * @param userId the ID of the user
     * @param domain the domain to filter the permissions by
     * @return a list of distinct permission names
     */
    @Query(value = "select distinct p.name from role_permission rp " +
            "join permission p on p.id = rp.permission_id and p.is_delete = false " +
            "join role r on r.id = rp.role_id and r.is_delete = false " +
            "where r.id in (select role_id from user_role where user_id = :userId) and p.name like :domain% and rp.is_delete = false;", nativeQuery = true)
    List<String> getPermissions(String userId, String domain);
}
