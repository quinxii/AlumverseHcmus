package hcmus.alumni.message.dto.response;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboxDto {
    private Long id;
    private String name;
    private boolean isGroup;
    private Date createAt;
    private Date updateAt;
    @JsonManagedReference
    private Set<InboxMemberDto> members;

}
