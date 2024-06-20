package hcmus.alumni.message.dto.request;

import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.model.MessageModel.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {
    private String senderId;
    private String content;
    private MessageType messageType;
    private Long parentMessageId;
}
