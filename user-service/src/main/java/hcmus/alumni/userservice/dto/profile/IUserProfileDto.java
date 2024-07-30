package hcmus.alumni.userservice.dto.profile;

import java.util.Date;

public interface IUserProfileDto {
	interface SexModel {
		Integer getId();
		String getName();
	}
	
	interface FacultyModel {
		Integer getId();
		String getName();
	}
	
    String getId();
    String getAvatarUrl();
    String getCoverUrl();
    String getFullName();
    FacultyModel getFaculty();
    SexModel getSex();
    Date getDob();
    String getSocialMediaLink();
    String getEmail();
    String getPhone();
    String getAboutMe();
}

