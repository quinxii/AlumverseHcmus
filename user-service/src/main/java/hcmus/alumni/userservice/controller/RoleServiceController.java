package hcmus.alumni.userservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @PreAuthorize("hasAuthority('User.Role.Create')")
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

    @PostMapping("")
    public ResponseEntity<String> postRole(@RequestBody RoleRequestDto requestingRole) {
        RoleModel role = new RoleModel(requestingRole);
        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putRole(@PathVariable Integer id, @RequestBody RoleRequestDto requestingRole) {
        RoleModel role = new RoleModel(requestingRole, id);
        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("")
    public ResponseEntity<String> putRoles(@RequestBody List<RoleModel> requestingRoles) {
        roleRepository.saveAll(requestingRoles);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Integer id) {
        roleRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
