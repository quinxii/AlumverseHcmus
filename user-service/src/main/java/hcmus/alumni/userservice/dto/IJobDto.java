package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface IJobDto {
	String getJobId();
    String getCompanyName();
    String getPosition();
    Date getStartTime();
    Date getEndTime();
    String getPrivacy();
    Boolean getIsDelete();
    Boolean getIsWorking();
}

