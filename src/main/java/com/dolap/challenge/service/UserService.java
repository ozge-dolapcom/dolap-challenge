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

    /**
     * Retrieves a user by the username
     *
     * @param username is the parameter to load the user by
     * @return the User object if found by the username provided
     */
    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    /**
     * Stores the user in the database with the given credentials
     *
     * @param username username of the User - will be used when logging in later
     * @param password password of the User - will be used when logging in later
     * @param role - whether ADMIN or USER. this defines what part of the app will be accessible by the user.
     *
     * @return User that just created in the database
     */
    public User register(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role == null ? User.ROLE_USER : role);

        return userRepository.save(user);
    }

    /**
     * Updates the role of the user found by the username
     *
     * @param username username of the User to update the role
     * @param newRole role of the User, whether ADMIN or USER
     * @return the updated User object
     */
    public User updateRole(String username, String newRole){
        User user = userRepository.getUserByUsername(username);
        user.setRole(newRole);
        return user;
    }
}
