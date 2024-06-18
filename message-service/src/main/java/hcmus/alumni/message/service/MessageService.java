package hcmus.alumni.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.model.MessageModel;

@Service
public class MessageService {
    @Autowired
    private InboxService inboxService;

    public MessageModel save(MessageModel message) {
        return null;
        
    }
}
