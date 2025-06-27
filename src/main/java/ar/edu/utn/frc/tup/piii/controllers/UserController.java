package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.user.PasswordChangeDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById (@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDto dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody @Valid PasswordChangeDto dto) {
        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }





}
