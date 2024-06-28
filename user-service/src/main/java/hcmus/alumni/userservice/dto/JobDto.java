package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface JobDto {
    String getUserId();
    String getCompanyName();
    String getPosition();
    Date getStartTime();
    Date getEndTime();
    String getPrivacy();
    Boolean getIsDelete();
    Boolean getIsWorking();
}

