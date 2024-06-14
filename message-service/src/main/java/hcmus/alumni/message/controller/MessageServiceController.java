package hcmus.alumni.message.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import hcmus.alumni.message.exception.AppException;
import hcmus.alumni.message.model.InboxMemberId;
import hcmus.alumni.message.model.InboxMemberModel;
import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.repostiory.InboxMemberRepository;
import hcmus.alumni.message.repostiory.InboxRepository;
import hcmus.alumni.message.repostiory.MessageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/messages")
public class MessageServiceController {
    @Autowired
    private InboxRepository inboxRepository;
    @Autowired
    private InboxMemberRepository inboxMemberRepository;
    @Autowired
    private MessageRepository messageRepository;

    @PreAuthorize("@inboxMemberRepository.existsById(new hcmus.alumni.message.model.InboxMemberId(#inboxId, #userId))")
    @GetMapping("/inbox/{inboxId}")
    public ResponseEntity<HashMap<String, Object>> getMethodName(
            @RequestHeader("userId") String userId,
            @PathVariable Long inboxId) {
        return ResponseEntity.ok(new HashMap<>() {
            {
                put("param", "dsa");
            }
        });
    }

    @PostMapping("/inbox")
    public ResponseEntity<HashMap<String, Object>> createInbox(
            @RequestHeader("userId") String userId,
            @RequestBody Set<InboxMemberModel> members // Exclude the one who creates the inbox
    ) {
        // Only create if there are no messages between the two users
        if (members.size() < 1) {
            throw new AppException(90100, "Thành viên không được để trống", HttpStatus.BAD_REQUEST);
        }

        members.add(new InboxMemberModel(new InboxMemberId(null, userId)));

        // Save the inbox first
        InboxModel inbox = new InboxModel();
        var savedInbox = inboxRepository.save(inbox);

        // Then save the members
        members.forEach(member -> member.setInbox(savedInbox));
        inboxMemberRepository.saveAll(members);

        return ResponseEntity.ok(new HashMap<>() {
            {
                put("inboxId", inbox.getId());
            }
        });
    }

    @GetMapping("")
    public ResponseEntity<HashMap<String, Object>> getMethodName() {
        System.out.println("Hello");
        return null;
    }

    @MessageMapping("/send-message/{inboxId}")
    @SendTo("/topic/public")
    public MessageModel sendMessage(
            @DestinationVariable Long inboxId,
            @Payload MessageModel message) {
        // Handle saving the message to the database
        // messageService.save(chatMessage);

        System.out.println(message);

        return message;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public MessageModel addUser(@Payload MessageModel message, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        return message;
    }
}
