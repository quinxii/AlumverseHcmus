package hcmus.alumni.userservice.dto;

import hcmus.alumni.userservice.model.UserModel;
import lombok.Data;

@Data
public class FriendRelationShipDto {
    private UserModel friend;
}
