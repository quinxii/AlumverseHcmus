package hcmus.alumni.userservice.dto;

import java.util.Date;
import java.util.List;

public interface IRoleDto {
    interface Permission {
        Integer getId();
        String getName();
    }

    Integer getId();
    String getName();
    String getDescription();
    Date getCreateAt();
    Date getUpdateAt();
    List<Permission> getPermissions();
}
