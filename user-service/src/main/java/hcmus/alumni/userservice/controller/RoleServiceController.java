package hcmus.alumni.userservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.userservice.dto.IRoleDto;
import hcmus.alumni.userservice.dto.RoleRequestDto;
import hcmus.alumni.userservice.model.PermissionModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.repository.RoleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // Allow requests from Web
@RequestMapping("/roles")
public class RoleServiceController {
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("")
    public ResponseEntity<HashMap<String, Object>> getRoles() {
        List<IRoleDto> roles = roleRepository.findAllRoles();
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("roles", roles);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IRoleDto> getRole(@PathVariable Integer id) {
        Optional<IRoleDto> role = roleRepository.findRoleById(id);
        if (role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(role.get());
    }

    @PreAuthorize("hasAuthority('User.Role.Create')")
    @PostMapping("")
    public ResponseEntity<String> postRole(@RequestBody RoleRequestDto requestingRole) {
        RoleModel role = new RoleModel(requestingRole);
        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasAuthority('User.Role.Edit')")
    @PutMapping("/{id}")
    public ResponseEntity<String> putRole(@PathVariable Integer id, @RequestBody RoleRequestDto requestingRole) {
        Optional<RoleModel> roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        RoleModel role = roleOptional.get();
        boolean isPut = false;

        if (requestingRole.getName() != null) {
            role.setName(requestingRole.getName());
            isPut = true;
        }
        if (requestingRole.getDescription() != null) {
            role.setDescription(requestingRole.getDescription());
            isPut = true;
        }
        if (requestingRole.getPermissions() != null) {
            role.getPermissions().clear();
            requestingRole.getPermissions().forEach(permission -> {
                role.getPermissions().add(new PermissionModel(permission.getId()));
            });
            isPut = true;
        }

        if (isPut) {
            roleRepository.save(role);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasAuthority('User.Role.Edit')")
    @PutMapping("")
    public ResponseEntity<String> putRoles(@RequestBody List<RoleModel> requestingRoles) {
        List<Integer> ids = requestingRoles.stream()
                .map(RoleModel::getId)
                .collect(Collectors.toList());
        List<RoleModel> roles = roleRepository.findByIds(ids);

        for (RoleModel role : roles) {
            for (RoleModel requestingRole : requestingRoles) {
                if (role.getId() == requestingRole.getId()) {
                    if (requestingRole.getName() != null) {
                        role.setName(requestingRole.getName());
                    }
                    if (requestingRole.getDescription() != null) {
                        role.setDescription(requestingRole.getDescription());
                    }
                    if (requestingRole.getPermissions() != null) {
                        role.getPermissions().clear();
                        requestingRole.getPermissions().forEach(permission -> {
                            role.getPermissions().add(new PermissionModel(permission.getId()));
                        });
                    }

                    requestingRoles.remove(requestingRole);
                    break;
                }
            }
        }

        roleRepository.saveAll(roles);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasAuthority('User.Role.Delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Integer id) {
        roleRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
