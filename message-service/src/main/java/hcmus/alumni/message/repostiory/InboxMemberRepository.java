package hcmus.alumni.message.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import hcmus.alumni.message.model.InboxMemberId;
import hcmus.alumni.message.model.InboxMemberModel;

public interface InboxMemberRepository extends JpaRepository<InboxMemberModel, InboxMemberId> {

}
