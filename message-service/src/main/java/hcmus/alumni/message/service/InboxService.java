package hcmus.alumni.message.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.message.dto.request.InboxRequestDto;
import hcmus.alumni.message.model.InboxMemberId;
import hcmus.alumni.message.model.InboxMemberModel;
import hcmus.alumni.message.model.InboxModel;
import hcmus.alumni.message.repostiory.InboxRepository;

/**
 * Service class for managing inbox-related operations.
 */
@Service
public class InboxService {
    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMemberService inboxMemberService;

    /**
     * Retrieves the latest inboxes for a given user.
     *
     * @param userId   the ID of the user
     * @param pageable the pagination information
     * @return a Page object containing the latest inboxes
     */
    public Page<InboxModel> getLastestInboxes(String userId, Pageable pageable) {
        return inboxRepository.getLatestInboxes(userId, pageable);
    }

    /**
     * Creates a new inbox with the provided details.
     *
     * @param req the InboxRequestDto object containing the details of the inbox to
     *            be created
     * @return the ID of the created inbox
     */
    @Transactional
    public Long createInbox(InboxRequestDto req) {
        // Create an inbox
        InboxModel inbox = new InboxModel();
        inbox.setName(req.getName());
        inbox.setIsGroup(req.getIsGroup());
        InboxModel savedInbox = inboxRepository.save(inbox);

        // Create members
        Set<InboxMemberModel> members = req.getMembers().stream()
                .map(member -> {
                    InboxMemberModel memberModel = new InboxMemberModel();
                    memberModel.setId(new InboxMemberId(savedInbox.getId(), member.getUserId()));
                    return memberModel;
                })
                .collect(Collectors.toSet());
        inboxMemberService.createMembers(members);

        return savedInbox.getId();
    }

    /**
     * Retrieves the individual inbox ID for the given user IDs.
     *
     * @param userId1 the first user ID
     * @param userId2 the second user ID
     * @return the individual inbox ID
     */
    public Long getIndividualInboxId(String userId1, String userId2) {
        return inboxRepository.getIndividualInboxId(userId1, userId2);
    }

    /**
     * Extracts user IDs from the given inbox, excluding the self user ID.
     *
     * @param inbox      The inbox model containing the members.
     * @param selfUserId The user ID to exclude.
     * @return A list of user IDs excluding the self user ID.
     */
    public List<String> extractUserIdsExcludeSelf(InboxModel inbox, String selfUserId) {
        return inbox.getMembers().stream()
                .filter(member -> !member.getId().getUserId().equals(selfUserId))
                .map(member -> member.getId().getUserId())
                .collect(Collectors.toList());
    }
}
