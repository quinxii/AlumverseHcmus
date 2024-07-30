package hcmus.alumni.userservice.dto.profile;

import java.util.Date;

import hcmus.alumni.userservice.common.Privacy;
import lombok.Data;

@Data
public class JobDto {
	String jobId;
    String companyName;
    String position;
    Date startTime;
    Date endTime;
    Privacy privacy;
    Boolean isDelete;
    Boolean isWorking;
}
