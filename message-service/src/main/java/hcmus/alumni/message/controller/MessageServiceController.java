package hcmus.alumni.message.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import hcmus.alumni.message.dto.request.InboxRequestDto;
import hcmus.alumni.message.dto.request.MessageRequestDto;
import hcmus.alumni.message.dto.response.InboxDto;
import hcmus.alumni.message.dto.response.MessageDto;
import hcmus.alumni.message.exception.AppException;
import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.service.InboxMemberService;
import hcmus.alumni.message.service.InboxService;
import hcmus.alumni.message.service.MessageService;
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
    private InboxService inboxService;
    @Autowired
    private InboxMemberService inboxMemberService;
    @Autowired
    private MessageService messageService;

    private static final int MAXIMUM_PAGES = 50;

    @GetMapping("/inbox")
    public ResponseEntity<HashMap<String, Object>> getInboxes(
            @RequestHeader("userId") String userId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
            pageSize = MAXIMUM_PAGES;
        }

        HashMap<String, Object> response = new HashMap<>();

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<InboxModel> inboxPage = inboxService.getLastestInboxes(userId, pageable);
        List<InboxModel> inboxes = inboxPage.getContent();
        messageService.processLatestInboxes(inboxes);

        response.put("totalPages", inboxPage.getTotalPages());
        response.put("inboxes", inboxes.stream().map(i -> mapper.map(i, InboxDto.class)).toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@inboxMemberRepository.existsById(new hcmus.alumni.message.model.InboxMemberId(#inboxId, #userId))")
    @GetMapping("/inbox/{inboxId}")
    public ResponseEntity<HashMap<String, Object>> getMessagesByInbox(
            @RequestHeader("userId") String userId,
            @PathVariable Long inboxId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
            pageSize = MAXIMUM_PAGES;
        }

        HashMap<String, Object> response = new HashMap<>();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<MessageModel> messages = messageService.getMessagesByInboxId(inboxId, pageable);

        response.put("totalPages", messages.getTotalPages());
        response.put("messages", messages.getContent().stream().map(m -> mapper.map(m, MessageDto.class)).toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/inbox")
    public ResponseEntity<HashMap<String, Object>> createInbox(
            @RequestHeader("userId") String userId,
            @RequestBody InboxRequestDto req) {
        // Only create if there are no messages between the two users
        if (req.getMembers().size() < 1) {
            throw new AppException(90300, "Thành viên không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (req.getIsGroup()) {
            if (req.getName() == null || req.getName().isBlank()) {
                throw new AppException(90301, "Tên nhóm không được để trống", HttpStatus.BAD_REQUEST);
            }
            if (req.getMembers().size() < 2) {
                throw new AppException(90302, "Nhóm phải có ít nhất 3 thành viên", HttpStatus.BAD_REQUEST);
            }
        } else {
            if (req.getMembers().size() > 1) {
                throw new AppException(90303, "Cuộc trò chuyện cá nhân không thể có hơn 2 thành viên",
                        HttpStatus.BAD_REQUEST);
            }
            for (InboxRequestDto.InboxMember member : req.getMembers()) {
                Long inboxId = inboxService.getIndividualInboxId(userId, member.getUserId());
                if (inboxId != null) {
                    HashMap<String, Object> response = new HashMap<>();
                    response.put("inboxId", inboxId);
                    return ResponseEntity.ok(response);
                }
            }
        }

        HashMap<String, Object> response = new HashMap<>();

        req.getMembers().add(new InboxRequestDto.InboxMember(userId));

        try {
            Long savedInboxId = inboxService.createInbox(req);
            response.put("inboxId", savedInboxId);
        } catch (JpaObjectRetrievalFailureException e) {
            throw new AppException(90301, "Người dùng không tồn tại", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/inbox/individual/{otherUserId}")
    public ResponseEntity<HashMap<String, Object>> getMethodName(
            @RequestHeader("userId") String userId,
            @PathVariable String otherUserId) {
        Long inboxId = inboxService.getIndividualInboxId(userId, otherUserId);
        if (inboxId == null) {
            throw new AppException(90405, "Không tìm thấy cuộc trò chuyện cá nhân", HttpStatus.NOT_FOUND);
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("inboxId", inboxId);

        return ResponseEntity.ok(response);
    }

    @Transactional
    @MessageMapping("/send-message/{inboxId}")
    public void sendMessage(
            @DestinationVariable Long inboxId,
            @Payload MessageRequestDto req) {
        // Handle saving the message to the database
        MessageModel savedMsg = messageService.saveFromReq(req, inboxId);

        // Get other members' userId in inbox
        List<String> userIds = inboxService.extractUserIdsExcludeSelf(savedMsg.getInbox(), req.getSenderId());
        Map<String, Object> response = new HashMap<>();
        response.put("inbox", mapper.map(savedMsg.getInbox(), InboxDto.class));
        response.put("message", mapper.map(savedMsg, MessageDto.class));

        for (String userId : userIds) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/messages",
                    response);
        }
    }
}
