package com.bilgeadam.basurveyapp.controller;

import com.bilgeadam.basurveyapp.dto.request.UserUpdateRequestDto;
import com.bilgeadam.basurveyapp.dto.response.UserResponseDto;
import com.bilgeadam.basurveyapp.dto.response.UserTrainersAndStudentsResponseDto;
import com.bilgeadam.basurveyapp.entity.User;
import com.bilgeadam.basurveyapp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/test")
    public String test() {
        return "hello";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/students")
    ResponseEntity<List<UserResponseDto>> getStudentList() {
        return ResponseEntity.ok(userService.getStudentList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/mastertrainers")
    ResponseEntity<List<UserResponseDto>> getMasterTrainersList() {
        return ResponseEntity.ok(userService.getMasterTrainerList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/assistanttrainers")
    ResponseEntity<List<UserResponseDto>> getAssistantTrainersList() {
        return ResponseEntity.ok(userService.getAssistantTrainerList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/managers")
    ResponseEntity<List<UserResponseDto>> getManagersList() {
        return ResponseEntity.ok(userService.getManagerList());
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admins")
    ResponseEntity<List<UserResponseDto>> getAdminsList() {
        return ResponseEntity.ok(userService.getAdminList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/page")
    ResponseEntity<Page<User>> getUserPage(Pageable pageable) {
        return ResponseEntity.ok(userService.getUserPage(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{userId}")
    ResponseEntity<User> findById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.findByOid(userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/update/{userId}")
    ResponseEntity<User> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequestDto dto) {
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/delete/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MASTER_TRAINER', 'ASSISTANT_TRAINER', 'STUDENT')")
    @GetMapping("/trainersandstudents")
    ResponseEntity<List<UserTrainersAndStudentsResponseDto>> getTrainersAndStudentsList(String jwtToken) {
        return ResponseEntity.ok(userService.getTrainersAndStudentsList(jwtToken).get());
    }
}
