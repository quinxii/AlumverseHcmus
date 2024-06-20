package hcmus.alumni.message.dto.request;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request DTO for an inbox.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboxRequestDto {
    /**
     * Represents a member of the inbox.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InboxMember {
        /**
         * Represents the user ID of the inbox's member.
         * The user ID must exclude the one who sends the request.
         */
        private String userId;
    }

    private String name;
    private Boolean isGroup = false;
    private Set<InboxMember> members;
}
