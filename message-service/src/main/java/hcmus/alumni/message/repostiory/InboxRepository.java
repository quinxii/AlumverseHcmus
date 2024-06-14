package hcmus.alumni.message.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.message.model.InboxModel;

public interface InboxRepository extends JpaRepository<InboxModel, Long>{

}
