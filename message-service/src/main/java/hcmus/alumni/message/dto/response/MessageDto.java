package hcmus.alumni.message.dto.response;

import java.util.Date;

import hcmus.alumni.message.model.MessageModel.MessageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private Long id;
    private InboxDto inbox;
    private UserDto sender;
    private String content;
    MessageType messageType;
    MessageDto parentMessage;
    Date createAt;
    Date updateAt;
    boolean isDelete;
}
