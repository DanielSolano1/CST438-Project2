package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final VocabListService vocabListService;

    public UserService(UserRepository repository, VocabListService vocabListService) {
        this.repository = repository;
        this.vocabListService = vocabListService;
    }
    // all of these are queries basically we need for functions
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return repository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }
    public Optional<User> getUserByPassword(String password) {
        return repository.findByPassword(password);
    }
    public Optional<User> getUserByEmailAndPassword(String email, String password) {
        return repository.findByEmailAndPassword(email, password);
    }

    public User createUser(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User created = repository.save(user);
        vocabListService.createList(created.getUserId(), "Vocab Word History");

        return created;
    }

    public void resetPassword(String email, String newPassword) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        user.setPassword(newPassword);
        repository.save(user);
    }


    public void deleteUser(Integer id) {
        repository.deleteById(id);
    }
}
