package com.dolap.challenge.service;


import com.dolap.challenge.entity.User;
import com.dolap.challenge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public User register(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role == null ? User.ROLE_USER : role);

        return userRepository.save(user);
    }

    public User updateRole(String username, String newRole){
        User user = userRepository.getUserByUsername(username);
        user.setRole(newRole);
        return user;
    }
}
