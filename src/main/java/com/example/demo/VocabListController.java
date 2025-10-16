package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/lists")
public class VocabListController {

    private final VocabListService vocabListService;

    public VocabListController(VocabListService vocabListService) {
        this.vocabListService = vocabListService;
    }

    @GetMapping
    public List<VocabList> getAllLists() {
        return vocabListService.getAllLists();
    }

    @GetMapping("/user/{userId}")
    public List<VocabList> getListsByUser(@PathVariable Integer userId) {
        return vocabListService.getListsByUser(userId);
    }

    @PostMapping
    public ResponseEntity<?> createList(@RequestParam Integer userId, @RequestParam String listName) {
        try {
            VocabList created = vocabListService.createList(userId, listName);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Integer id) {
        vocabListService.deleteList(id);
        return ResponseEntity.noContent().build();
    }
}
