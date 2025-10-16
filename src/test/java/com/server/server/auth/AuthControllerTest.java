package com.server.server.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User("brucewayne", "123what", "waynenterprise@gmail.com", true, LocalDateTime.now());
        mockUser.setRole("ADMIN");
    }



    @Test
    void testLogin_Successful() {
        AuthRequest req = new AuthRequest("brucewayne", "Passwordlol");

        when(userRepo.findByUsername("brucewayne")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Passwordlol", "123what")).thenReturn(true);
        when(jwtUtil.generateToken("brucewayne", "ADMIN")).thenReturn("mockToken");

        ResponseEntity<?> response = authController.login(req);

        assertEquals(200, response.getStatusCodeValue());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertEquals("brucewayne", body.username());
        assertEquals("ADMIN", body.role());
        assertEquals("mockToken", body.token());
    }

    @Test
    void testLogin_InvalidPassword() {
        AuthRequest req = new AuthRequest("brucewayne", "wrong");

        when(userRepo.findByUsername("brucewayne")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "123what")).thenReturn(false);

        ResponseEntity<?> response = authController.login(req);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void testLogin_InactiveUser() {
        mockUser.setActive(false);
        AuthRequest req = new AuthRequest("brucewayne", "Passwordlol");

        when(userRepo.findByUsername("brucewayne")).thenReturn(Optional.of(mockUser));

        ResponseEntity<?> response = authController.login(req);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(((String) response.getBody()).contains("inactive"));
    }

    @Test
    void testLogin_UserNotFound() {
        AuthRequest req = new AuthRequest("ghost", "whatever");
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(req);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void testCreateUser_Successful() {
        User newUser = new User("ariel", "Pass2", "ariel@yahoo.com", true, LocalDateTime.now());
        when(passwordEncoder.encode("Pass2")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = authController.createUser(newUser);

        assertEquals(200, response.getStatusCodeValue());
        User saved = (User) response.getBody();
        assertTrue(saved.isActive());
        assertEquals("encodedPass", saved.getPassword());
        assertNotNull(saved.getCreatedDate());
        assertEquals("VIEWER", saved.getRole()); // default role
    }


    @Test
    void testUpdateUserActiveStatus_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

        ResponseEntity<?> response = authController.updateUserActiveStatus(1L, false);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User active status updated", response.getBody());
        verify(userRepo).save(mockUser);
        assertFalse(mockUser.isActive());
    }

    @Test
    void testUpdateUserActiveStatus_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.updateUserActiveStatus(1L, false);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }


    @Test
    void testUpdateUser_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        User updateData = new User();
        updateData.setUsername("newname");
        updateData.setPassword("newPass");
        updateData.setEmail("new@gmail.com");
        updateData.setActive(true);

        ResponseEntity<?> response = authController.updateUser(1L, updateData);
        assertEquals(200, response.getStatusCodeValue());

        User updated = (User) response.getBody();
        assertEquals("newname", updated.getUsername());
        assertEquals("encodedNew", updated.getPassword());
        assertEquals("new@gmail.com", updated.getEmail());
        verify(userRepo).save(mockUser);
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = authController.updateUser(1L, new User());
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetAllUsers_ReturnsList() {
        when(userRepo.findAll()).thenReturn(List.of(mockUser));
        ResponseEntity<List<User>> response = authController.getAllUsers();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("brucewayne", response.getBody().get(0).getUsername());
    }
}
