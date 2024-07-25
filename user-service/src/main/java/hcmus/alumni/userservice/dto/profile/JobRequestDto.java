package hcmus.alumni.userservice.dto.profile;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestDto {
    private String companyName;
    private String position;
    private Date startTime;
    private Date endTime;
    private String privacy;
    private Boolean isDelete;
    private Boolean isWorking = false;
}
