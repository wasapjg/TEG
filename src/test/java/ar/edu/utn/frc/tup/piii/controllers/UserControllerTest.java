package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("hashedPassword123")
                .lastLogin(LocalDateTime.now())
                .build();

        User testUser2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@email.com")
                .passwordHash("hashedPassword456")
                .lastLogin(LocalDateTime.now())
                .build();

        testUsers = Arrays.asList(testUser, testUser2);
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        when(userService.getAllUsers()).thenReturn(testUsers);

        mockMvc.perform(get("/api/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@email.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("testuser2"));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUser);

        mockMvc.perform(get("/api/user/1")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldReturnBadRequest() throws Exception {
        Long userId = 999L;
        when(userService.getUserById(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

}
