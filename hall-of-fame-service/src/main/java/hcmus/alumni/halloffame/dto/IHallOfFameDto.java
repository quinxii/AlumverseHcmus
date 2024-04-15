package hcmus.alumni.halloffame.dto;

import java.util.Date;

public interface IHallOfFameDto {
    interface User {
        String getId(); // Added
        String getFullName();
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
    String getContent();
    String getThumbnail();
    Integer getViews();
    Date getCreateAt(); // Added
    Date getUpdateAt();
    Date getPublishedAt();
    User getCreator();
    User getUserId();
    Faculty getFaculty();
    StatusPost getStatus();
    Integer getBeginningYear();
}
