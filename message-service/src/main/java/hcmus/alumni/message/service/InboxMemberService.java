package hcmus.alumni.message.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.model.InboxMemberId;
import hcmus.alumni.message.model.InboxMemberModel;
import hcmus.alumni.message.repostiory.InboxMemberRepository;

@Service
public class InboxMemberService {
    @Autowired
    private InboxMemberRepository inboxMemberRepository;

    /**
     * Creates multiple inbox members.
     *
     * @param members a set of InboxMemberModel objects representing the members to
     *                be created
     */
    public void createMembers(Set<InboxMemberModel> members) {
        inboxMemberRepository.saveAll(members);
    }

    /**
     * Checks if an inbox member with the specified ID exists.
     *
     * @param id the ID of the inbox member to check
     * @return true if an inbox member with the specified ID exists, false otherwise
     */
    public boolean existsById(InboxMemberId id) {
        return inboxMemberRepository.existsById(id);
    }
}
