package com.example.demo;

import org.springframework.http.HttpStatus;
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

    // ✅ Get all words in a list
    @GetMapping("/list/{listId}")
    public List<WordsInList> getWordsByList(@PathVariable Integer listId) {
        return wordsInListService.getWordsByList(listId);
    }

    // ✅ Add new word to list (with duplicate prevention)
    @PostMapping
    public ResponseEntity<?> addWordToList(@RequestBody WordRequest request) {
        try {
            WordsInList entry = wordsInListService.addWordToList(
                    request.getUserId(),
                    request.getListId(),
                    request.getWord(),
                    request.getDefinition()
            );
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


    @GetMapping
    public List<WordsInList> getAllWords() {
        return wordsInListService.getAllWords();
    }

    // Get words by user
    @GetMapping("/user/{userId}")
    public List<WordsInList> getWordsByUser(@PathVariable Integer userId) {
        return wordsInListService.getWordsByUser(userId);
    }

    // Alternate POST style: include listId in the path
    // Keeps your original POST as-is; this is just another option for the frontend.
    @PostMapping("/list/{listId}")
    public ResponseEntity<?> addWordToListPathParam(
            @PathVariable Integer listId,
            @RequestBody WordPathRequest request
    ) {
        try {
            WordsInList entry = wordsInListService.addWordToList(
                    request.getUserId(),
                    listId,
                    request.getWord(),
                    request.getDefinition()
            );
            // Use 201 for creation on this variant
            return ResponseEntity.status(HttpStatus.CREATED).body(entry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DTO to receive request body (existing)
    public static class WordRequest {
        private Integer userId;
        private Integer listId;
        private String word;
        private String definition;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public Integer getListId() { return listId; }
        public void setListId(Integer listId) { this.listId = listId; }
        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }
        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }
    }

    // 🆕 DTO for the path-based POST (no listId in body needed)
    public static class WordPathRequest {
        private Integer userId;
        private String word;
        private String definition;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }
        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }
    }
}
