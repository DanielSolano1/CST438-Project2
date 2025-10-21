package com.example.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class Controller {

    private final UserRepository userRepository;
    WordsInListRepository WordsInListRepository;
    VocabListRepository VocabListRepository;

    public Controller(UserRepository userRepository,VocabListRepository VocabListRepository, WordsInListRepository WordsInListRepository) {
        this.userRepository = userRepository;
        this.VocabListRepository = VocabListRepository;
        this.WordsInListRepository = WordsInListRepository;

    }


    @GetMapping("/")//Basic route for testing stuff
    public String home(){
        return "Hello World";
    }

    //From here on will be routes that may be able to be used in theory,
    //still trying to figure out where the API calls are in the front end

    @GetMapping("/word_def/?")
    public String word_def(String word) {
        return word + ": def";
    }

    //Honestly, going to have to mess with this one a bunch


    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by ID
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Check if email exists (example of using your custom methods)
    @GetMapping("/users/check-email")
    public boolean emailExists(@RequestParam String email) {
        return userRepository.existsByEmail(email);
    }

    //Get all the vocabulary list
    @GetMapping("/vocabList")
    public List<VocabList> getAllVocabList() {
        return VocabListRepository.findAll();
    }




}
