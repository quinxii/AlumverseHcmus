package hcmus.alumni.group.dto;

import java.util.Date;

public interface IGroupMemberDto {
	interface User {
		String getId();
		String getFullName();
		String getAvatarUrl();
    }
    
    interface Group {
    	String getId();
        String getName();
        String getCoverUrl();
    }

    User getUser();
    Group getGroup();
    String getRole();
}
