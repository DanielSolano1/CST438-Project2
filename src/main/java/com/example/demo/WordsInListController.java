package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/words")
public class WordsInListController {

    private final WordsInListService wordsInListService;

    public WordsInListController(WordsInListService wordsInListService) {
        this.wordsInListService = wordsInListService;
    }

    @GetMapping
    public List<WordsInList> getAllWords() {
        return wordsInListService.getAllWords();
    }

    @GetMapping("/user/{userId}")
    public List<WordsInList> getWordsByUser(@PathVariable Integer userId) {
        return wordsInListService.getWordsByUser(userId);
    }

    @GetMapping("/list/{listId}")
    public List<WordsInList> getWordsByList(@PathVariable Integer listId) {
        return wordsInListService.getWordsByList(listId);
    }

    @PostMapping
    public ResponseEntity<?> addWordToList(
            @RequestParam Integer userId,
            @RequestParam Integer listId,
            @RequestParam String word,
            @RequestParam String definition
    ) {
        try {
            WordsInList entry = wordsInListService.addWordToList(userId, listId, word, definition);
            return ResponseEntity.ok(entry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Integer wordId) {
        wordsInListService.deleteWord(wordId);
        return ResponseEntity.noContent().build();
    }
}
