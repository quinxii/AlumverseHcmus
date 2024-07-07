
package hcmus.alumni.message.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.model.InboxReadStatusId;
import hcmus.alumni.message.model.InboxReadStatusModel;
import hcmus.alumni.message.repostiory.InboxReadStatusRepository;

@Service
public class InboxReadStatusService {

    @Autowired
    private InboxReadStatusRepository inboxReadStatusRepository;

    /**
     * Processes the read status of the given list of inboxes for a specific user.
     *
     * @param inboxes The list of inboxes to process.
     * @param userId The ID of the user.
     */
    public void processReadStatus(List<InboxModel> inboxes, String userId) {
        List<InboxReadStatusId> readStatusIds = new ArrayList<>();
        for (InboxModel inbox : inboxes) {
            InboxReadStatusId id = new InboxReadStatusId(inbox.getId(), userId);
            readStatusIds.add(id);
        }
        Set<Long> lastReadMessageIds = inboxReadStatusRepository.getLastReadMessageIds(readStatusIds);

        inboxes.forEach(inbox -> {
            if (lastReadMessageIds.contains(inbox.getLatestMessage().getId())) {
                inbox.setHasRead(true);
            } else {
                inbox.setHasRead(false);
            }
        });
    }

    /**
     * Updates the last read message for a given inbox and user.
     *
     * @param inboxId   the ID of the inbox
     * @param userId    the ID of the user
     * @param messageId the ID of the last read message
     */
    public void updateLastReadMessage(Long inboxId, String userId, Long messageId) {
        InboxReadStatusId id = new InboxReadStatusId(inboxId, userId);
        InboxReadStatusModel inboxReadStatus = new InboxReadStatusModel(id, messageId);
        inboxReadStatusRepository.save(inboxReadStatus);
    }
}