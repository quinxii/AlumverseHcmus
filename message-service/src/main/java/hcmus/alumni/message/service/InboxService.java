package hcmus.alumni.message.service;

import java.util.List;
import java.util.Optional;
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
     * Retrieves an inbox model by its ID.
     *
     * @param id the ID of the inbox model to retrieve
     * @return an Optional containing the inbox model if found, otherwise an empty Optional
     */
    public Optional<InboxModel> findById(Long id) {
        return inboxRepository.findById(id);
    }

    /**
     * Retrieves the latest inboxes for a given user with optional search query.
     * The search is performed based on the full name of the user who is in inbox of
     * the provided user ID.
     *
     * @param userId   the ID of the user
     * @param query    the search query (can be null or empty)
     * @param pageable the pagination information
     * @return a Page object containing the latest inboxes
     */
    public Page<InboxModel> getLastestInboxes(String userId, String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return inboxRepository.getLatestInboxes(userId, pageable);
        } else {
            return inboxRepository.getLatestInboxesWithSearch(userId, query, pageable);
        }
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
     * Extracts user IDs from the given inbox.
     *
     * @param inbox The inbox model containing the members.
     * @return A list of user IDs.
     */
    public List<String> extractUserIds(InboxModel inbox) {
        return inbox.getMembers().stream()
                .map(member -> member.getId().getUserId())
                .collect(Collectors.toList());
    }
}
