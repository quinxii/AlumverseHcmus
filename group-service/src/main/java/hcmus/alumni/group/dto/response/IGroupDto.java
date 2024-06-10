package hcmus.alumni.group.dto.response;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.group.common.Privacy;
import hcmus.alumni.group.common.GroupMemberRole;

public interface IGroupDto {
    interface User {
		String getId();
		String getFullName();
		String getAvatarUrl();
    }
    
    interface Status {
        String getName();
    }
    
    interface Tag{
		Integer getId();
		String getName();
	}
    
    interface Permissions {
        Boolean getEdit();
        Boolean getDelete();
    }

    String getId();
    String getName();
    User getCreator();
    String getDescription();
    String getType();
    String getCoverUrl();
    String getWebsite();
    Privacy getPrivacy();
    Status getStatus();
    Set<Tag> getTags();
    Date getUpdateAt();
    Date getCreateAt();
    Integer getParticipantCount();
    GroupMemberRole getUserRole();
    boolean getIsRequestPending();
    Permissions getPermissions();
}

