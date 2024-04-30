package hcmus.alumni.group.dto;

public interface IRequestJoinGroupDto {
	interface User {
		String getId();
        String getFullName();
        String getAvatarUrl();
    }
    
    interface Group {
    	String getId();
        String getName();
        String getAvatarUrl();
    }
    
    User getUser();
    Group getGroup();
}
