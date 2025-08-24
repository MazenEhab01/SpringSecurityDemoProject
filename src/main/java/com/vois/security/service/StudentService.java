package com.vois.security.service;

import com.vois.security.dto.StudentRequest;
import com.vois.security.dto.StudentResponse;
import com.vois.security.entity.Role;
import com.vois.security.entity.Student;
import com.vois.security.entity.User;
import com.vois.security.exception.StudentNotFoundException;
import com.vois.security.exception.UserAlreadyExistsException;
import com.vois.security.repository.StudentRepository;
import com.vois.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        return convertToResponse(student);
    }


    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));

        // Delete associated user as well
        User user = student.getUser();
        studentRepository.delete(student);
        userRepository.delete(user);
    }

    private StudentResponse convertToResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCourse(),
                student.getAcademic_year()
        );
    }
}

