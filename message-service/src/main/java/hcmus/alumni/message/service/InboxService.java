package hcmus.alumni.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.repostiory.InboxRepository;

@Service
public class InboxService {
    @Autowired
    private InboxRepository inboxRepository;
}
