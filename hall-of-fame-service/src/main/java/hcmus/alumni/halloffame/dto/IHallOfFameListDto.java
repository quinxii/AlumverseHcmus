package hcmus.alumni.halloffame.dto;

import java.util.Date;

public interface IHallOfFameListDto {
    interface User {
        String getId(); // Added
        String getFullName();
    }
    interface LinkedUser {
        String getId();
        String getFullName();
        String getEmail();
    }

    interface StatusPost {
        Integer getId(); // Added
        String getName();
    }

    interface Faculty {
        Integer getId();
        String getName();
    }

    String getId();
    String getTitle();
    String getSummary();
    String getThumbnail();
    Integer getViews();
    Date getCreateAt(); // Added
    Date getUpdateAt();
    Date getPublishedAt();
    User getCreator();
    LinkedUser getLinkedUser();
    Faculty getFaculty();
    StatusPost getStatus();
    Integer getBeginningYear();
    String getPosition();
}
