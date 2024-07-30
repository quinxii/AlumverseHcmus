package hcmus.alumni.authservice.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "[role_permission]")
public class RolePermissionModel implements Serializable {

    @EmbeddedId
    private RolePermissionId id;

    @Column(name = "is_delete")
    private Boolean isDelete;

    // Constructors, getters, and setters
}

@Embeddable
class RolePermissionId implements Serializable {
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "permission_id")
    private Integer permissionId;

    // Constructors, getters, and setters
}
