package hcmus.alumni.message.repostiory;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.message.model.InboxReadStatusId;
import hcmus.alumni.message.model.InboxReadStatusModel;

public interface InboxReadStatusRepository extends JpaRepository<InboxReadStatusModel, InboxReadStatusId> {
    @Query("SELECT irs.lastReadMessageId FROM InboxReadStatusModel irs WHERE irs.id IN :ids")
    Set<Long> getLastReadMessageIds(List<InboxReadStatusId> ids);
}
