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
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Inbox {
        private Long id;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sender {
        private String id;
    }

    private Inbox inbox;
    private Sender sender;
    private String content;
    private MessageType messageType;
    private MessageModel parentMessage;
}
