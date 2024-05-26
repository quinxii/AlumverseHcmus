package hcmus.alumni.event.dto;

import java.util.Date;
import java.util.Set;

public interface IEventDto {
	interface User{
		String getId();
		String getFullName();
		String getAvatarUrl();
	}
	interface StatusPost{
		String getName();
	}
	interface Tag{
		Integer getId();
		String getName();
	}
	interface Faculty{
		Integer getId();
		String getName();
	}
	interface Permissions {
        Boolean getEdit();
        Boolean getDelete();
    }

	String getId();
	String getTitle();
	String getContent();
	String getThumbnail();
	String getOrganizationLocation();
	Date getOrganizationTime();
	Integer getViews();
	Date getUpdateAt();
	Date getPublishedAt();
	User getCreator();
	Set<Tag> getTags();
	Faculty getFaculty();
	StatusPost getStatus();
	Integer getParticipants();
	Integer getMinimumParticipants();
	Integer getMaximumParticipants();
	Integer getChildrenCommentNumber();
	Permissions getPermissions();
}
