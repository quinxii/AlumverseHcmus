package hcmus.alumni.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.repostiory.InboxMemberRepository;

@Service
public class InboxMemberService {
    @Autowired
    private InboxMemberRepository inboxMemberRepository;
}
