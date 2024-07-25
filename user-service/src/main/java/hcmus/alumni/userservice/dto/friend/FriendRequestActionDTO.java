package hcmus.alumni.userservice.dto.friend;

import hcmus.alumni.userservice.model.FriendRequestAction;
import lombok.Data;

@Data
public class FriendRequestActionDTO {
    private String friendId;
    private FriendRequestAction action;
}