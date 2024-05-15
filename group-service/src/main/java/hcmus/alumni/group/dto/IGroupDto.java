package hcmus.alumni.group.dto;

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
    String getAvatarUrl();
    String getCoverUrl();
    String getWebsite();
    Privacy getPrivacy();
    Status getStatus();
}

