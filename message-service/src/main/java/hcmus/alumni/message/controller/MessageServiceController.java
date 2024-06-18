package hcmus.alumni.message.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import hcmus.alumni.message.dto.request.MessageRequestDto;
import hcmus.alumni.message.dto.response.MessageDto;
import hcmus.alumni.message.exception.AppException;
import hcmus.alumni.message.model.InboxMemberId;
import hcmus.alumni.message.model.InboxMemberModel;
import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.repostiory.InboxMemberRepository;
import hcmus.alumni.message.repostiory.InboxRepository;
import hcmus.alumni.message.repostiory.MessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/messages")
public class MessageServiceController {
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InboxRepository inboxRepository;
    @Autowired
    private InboxMemberRepository inboxMemberRepository;
    @Autowired
    private MessageRepository messageRepository;

    private static final int MAXIMUM_PAGES = 50;

    @PreAuthorize("@inboxMemberRepository.existsById(new hcmus.alumni.message.model.InboxMemberId(#inboxId, #userId))")
    @GetMapping("/inbox/{inboxId}")
    public ResponseEntity<HashMap<String, Object>> getMethodName(
            @RequestHeader("userId") String userId,
            @PathVariable Long inboxId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
            pageSize = MAXIMUM_PAGES;
        }

        HashMap<String, Object> response = new HashMap<>();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<MessageModel> messages = messageRepository.findByInboxId(inboxId, pageable);

        response.put("totalPages", messages.getTotalPages());
        response.put("messages", messages.getContent().stream().map(m -> mapper.map(m, MessageDto.class)).toList());
        return ResponseEntity.ok(response);
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
        HashMap<String, Object> response = new HashMap<>();

        members.add(new InboxMemberModel(new InboxMemberId(null, userId)));

        // Save the inbox first
        InboxModel inbox = new InboxModel();
        var savedInbox = inboxRepository.save(inbox);

        // Then save the members
        members.forEach(member -> member.setInbox(savedInbox));
        inboxMemberRepository.saveAll(members);

        response.put("inboxId", savedInbox.getId());

        return ResponseEntity.ok(response);
    }

    @Transactional
    @MessageMapping("/send-message/{inboxId}")
    public void sendMessage(
            @DestinationVariable Long inboxId,
            @Payload MessageRequestDto req) {
        // Handle saving the message to the database
        MessageModel msg = new MessageModel(req);
        msg.setInbox(new InboxModel(inboxId));

        MessageModel savedMsg = messageRepository.saveAndFlush(msg);
        entityManager.refresh(savedMsg);

        // Get other members' userId in inbox
        List<String> userIds = savedMsg.getInbox().getMembers().stream()
                .filter(member -> !member.getId().getUserId().equals(req.getSender().getId()))
                .map(member -> member.getId().getUserId())
                .collect(Collectors.toList());

        for (String userId : userIds) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/messages",
                    mapper.map(savedMsg, MessageDto.class));
        }
    }
}
