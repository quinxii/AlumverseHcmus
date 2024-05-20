package hcmus.alumni.group.dto;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.group.common.Privacy;

public interface IGroupDto {
    interface User {
		String getId();
		String getFullName();
		String getAvatarUrl();
    }
    
    interface Status {
        String getName();
    }

    String getId();
    String getName();
    User getCreator();
    String getType();
    String getCoverUrl();
    String getWebsite();
    Privacy getPrivacy();
    Status getStatus();
    Date getUpdateAt();
    Date getCreateAt();
    Integer getParticipantCount();
    boolean getIsJoined();
    boolean getIsRequestPending();
}

