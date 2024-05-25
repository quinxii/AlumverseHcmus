package hcmus.alumni.userservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.userservice.dto.IPermissionDto;
import hcmus.alumni.userservice.dto.IRoleDto;
import hcmus.alumni.userservice.dto.IRoleWithoutPermissionsDto;
import hcmus.alumni.userservice.dto.RoleRequestDto;
import hcmus.alumni.userservice.model.PermissionModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.repository.PermissionRepository;
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
    @Autowired
    private PermissionRepository permissionRepository;

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public void handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        System.out.println(name + " parameter is missing");
        // Actual exception handling
    }

    @GetMapping("/permissions")
    public ResponseEntity<HashMap<String, Object>> getAllPermissions() {
        List<IPermissionDto> permissions = permissionRepository.findAllPermissions();
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("permissions", permissions);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("")
    public ResponseEntity<HashMap<String, Object>> getRoles(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orderBy", required = false, defaultValue = "id") String orderBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order) {
        HashMap<String, Object> result = new HashMap<String, Object>();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
        Page<IRoleWithoutPermissionsDto> roles = roleRepository.searchRoles(name, pageable);

        result.put("totalPages", roles.getTotalPages());
        result.put("roles", roles.getContent());
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
            role.clearPermissions();
            requestingRole.getPermissions().forEach(permission -> {
                role.addPermission(new PermissionModel(permission.getId()));
            });
            isPut = true;
        }

        if (isPut) {
            roleRepository.save(role);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasAuthority('User.Role.Delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Integer id) {
        roleRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
