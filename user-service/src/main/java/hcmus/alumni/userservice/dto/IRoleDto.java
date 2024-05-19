package hcmus.alumni.userservice.dto;

import java.util.List;

public interface IRoleDto {
    interface Permission {
        Integer getId();
        String getName();
    }

    Integer getId();
    String getName();
    List<Permission> getPermissions();
}
