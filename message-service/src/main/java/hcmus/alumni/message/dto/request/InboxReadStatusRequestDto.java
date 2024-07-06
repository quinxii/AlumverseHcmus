package hcmus.alumni.message.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboxReadStatusRequestDto {
    private String userId;
    private Long lastReadMessageId;
}
