package hcmus.alumni.message.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import hcmus.alumni.message.model.InboxMemberModel.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboxMemberDto {
    @Getter
    @Setter
    public static class Id {
        private Long inboxId;
        private String userId;
    }

    private Id id;
    @JsonBackReference
    private InboxDto inbox;
    private UserDto user;
    private Role role;
    private Date joinedAt;
    private boolean isDelete;
}
