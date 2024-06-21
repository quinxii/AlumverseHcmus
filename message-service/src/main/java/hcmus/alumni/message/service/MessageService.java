package hcmus.alumni.message.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.message.dto.request.MessageRequestDto;
import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.model.MessageModel.MessageType;
import hcmus.alumni.message.model.UserModel;
import hcmus.alumni.message.repostiory.MessageRepository;
import jakarta.persistence.EntityManager;

/**
 * This class represents a service for sending messages.
 */
@Service
public class MessageService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ImageService imageService;

    /**
     * Processes the latest inboxes by retrieving the latest messages for each inbox
     * and updating the corresponding inbox with the latest message.
     *
     * @param inboxes the list of inboxes to process
     */
    public void processLatestInboxes(List<InboxModel> inboxes) {
        List<Long> inboxIds = inboxes.stream().map(InboxModel::getId).toList();
        List<MessageModel> latestMessages = messageRepository.getLatestMessagesByInboxIds(inboxIds);

        Map<Long, MessageModel> latestMessagesMap = latestMessages.stream()
                .collect(Collectors.toMap(msg -> msg.getInbox().getId(), msg -> msg));

        inboxes.forEach(inbox -> {
            MessageModel latestMessage = latestMessagesMap.get(inbox.getId());
            if (latestMessage != null) {
                inbox.setLatestMessage(latestMessage);
            }
        });
    }

    /**
     * Retrieves a page of messages by the given inbox ID.
     *
     * @param inboxId  the ID of the inbox to retrieve messages from
     * @param pageable the pagination information {@link Pageable}
     * @return a page of {@link MessageModel} objects
     */
    public Page<MessageModel> getMessagesByInboxId(Long inboxId, Pageable pageable) {
        return messageRepository.getByInboxId(inboxId, pageable);
    }

    /**
     * Saves a message from the given request and associates it with the specified
     * inbox.
     *
     * @param req     The message request DTO containing the details of the message.
     * @param inboxId The ID of the inbox to associate the message with.
     * @return The saved message model.
     */
    @Transactional
    public MessageModel saveFromReq(MessageRequestDto req, Long inboxId) {
        // Handle saving the message to the database
        MessageModel msg = new MessageModel(req);
        msg.setInbox(new InboxModel(inboxId));

        MessageModel savedMsg = messageRepository.saveAndFlush(msg);
        entityManager.refresh(savedMsg);

        return savedMsg;
    }

    @Transactional
    public MessageModel saveMediaTypeMsg(Long inboxId, String senderId, MultipartFile file, MessageType messageType,
            Long parentMessageId) throws IOException {
        String mediaUrl = imageService.saveImageToStorage(ImageService.messagesPath, file,
                ImageService.generateUniqueImageName(inboxId));

        MessageModel msg = new MessageModel();
        msg.setInbox(new InboxModel(inboxId));
        msg.setSender(new UserModel(senderId));
        msg.setContent(mediaUrl);
        msg.setMessageType(messageType);
        msg.setParentMessage(parentMessageId != null ? new MessageModel(parentMessageId) : null);

        MessageModel savedMsg = messageRepository.saveAndFlush(msg);
        entityManager.refresh(savedMsg);

        return savedMsg;
    }
}
