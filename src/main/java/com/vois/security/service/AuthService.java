package com.vois.security.service;

import com.vois.security.dto.AuthResponse;
import com.vois.security.dto.LoginRequest;
import com.vois.security.dto.RegisterRequest;
import com.vois.security.entity.Role;
import com.vois.security.entity.Student;
import com.vois.security.dto.StudentResponse;
import com.vois.security.entity.User;
import com.vois.security.exception.UserAlreadyExistsException;
import com.vois.security.repository.StudentRepository;
import com.vois.security.repository.UserRepository;
import com.vois.security.security.JwtUtil;
import com.vois.security.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        // If role is STUDENT, create student record
        if (request.getRole() == Role.STUDENT) {
            if (request.getCourse() == null || request.getAcademic_year() == null) {
                throw new IllegalArgumentException("Course and year are required for student registration");
            }

            Student student = new Student();
            student.setName(request.getName());
            student.setEmail(request.getEmail());
            student.setCourse(request.getCourse());
            student.setAcademic_year(request.getAcademic_year());
            student.setUser(savedUser);

            studentRepository.save(student);
        }

        // Generate token
        UserDetails userDetails = new UserDetailsImpl(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, savedUser.getEmail(), savedUser.getName(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userDetails.getUser();
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}