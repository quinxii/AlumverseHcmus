package hcmus.alumni.userservice.dto.role;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequestDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionRequestDto {
        private Integer id;
    }

    private String name;
    private String description;
    Set<PermissionRequestDto> permissions;
}
