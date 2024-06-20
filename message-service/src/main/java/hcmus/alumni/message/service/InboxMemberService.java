package hcmus.alumni.message.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.model.InboxMemberModel;
import hcmus.alumni.message.repostiory.InboxMemberRepository;

@Service
public class InboxMemberService {
    @Autowired
    private InboxMemberRepository inboxMemberRepository;

    public void createMembers(Set<InboxMemberModel> members) {
        inboxMemberRepository.saveAll(members);
    }
}
