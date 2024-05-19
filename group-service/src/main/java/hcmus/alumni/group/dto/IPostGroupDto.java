package hcmus.alumni.group.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IPostGroupDto {
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
	interface Pictures {
		String getId();
		String getPictureUrl();
	}
	interface Permissions {
        Boolean getEdit();
        Boolean getDelete();
    }
	
	String getId();
	User getCreator();
	String getTitle();
	String getContent();
	List<Pictures> getPictures();
	String getGroupId();
	Set<Tag> getTags();
	Date getUpdateAt();
	Date getPublishedAt();
	StatusPost getStatus();
	Integer getChildrenCommentNumber();
	Integer getReactionCount();
	Boolean getIsReacted();
	Permissions getPermissions();
}
