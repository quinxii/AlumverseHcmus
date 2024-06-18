package hcmus.alumni.message.repostiory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.message.model.MessageModel;

public interface MessageRepository extends JpaRepository<MessageModel, Long> {
    Page<MessageModel> findByInboxId(Long inboxId, Pageable pageable);
}
