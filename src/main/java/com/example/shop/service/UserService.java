package com.example.shop.service;

import com.example.shop.model.User;
import com.example.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void registerUser(User user) {
        userRepository.save(user);

        User.Role userRole = new User.Role();
        userRole.setRole("USER");
        userRole.setBalance(2000.0);
        userRole.setUser(user);

        Set<User.Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);


        userRepository.save(user);
    }


    public Double getInjectedUserBalanceById(Long userId) {
        try {
            List<Double> balances = userRepository.findInjectedBalanceById(userId);
            return balances.isEmpty() ? null : balances.get(0);
        } catch (Exception e) {
            logger.error("Ошибка при получении баланса по ID (инъекция): {}", e.getMessage());
            return null;
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Getting user by ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info("User found: {}", user.get());
            logger.info("User roles: {}", user.get().getRoles());
        } else {
            logger.warn("User with ID {} not found", id);
        }
        return user;
    }

    @Transactional
    public void updateUserBalanceById(Long userId, double newBalance) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                user.getRoles().iterator().next().setBalance(newBalance);
                userRepository.save(user);
            } else {
                logger.error("User with ID {} has no roles", user.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no roles");
            }
        } else {
            logger.error("User with ID {} not found", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        logger.info("User found: {}", user.getEmail());
        logger.info("Roles for user: {}", user.getRoles());

        Set<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(this::mapRoleToAuthority)
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }

    private SimpleGrantedAuthority mapRoleToAuthority(User.Role role){
        return new SimpleGrantedAuthority(role.getRole());
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}