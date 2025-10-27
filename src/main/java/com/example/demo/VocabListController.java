package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/lists")
public class VocabListController {

    private final VocabListService vocabListService;

    public VocabListController(VocabListService vocabListService) {
        this.vocabListService = vocabListService;
    }

    // ✅ Get all lists for a user (with optional exclusion)
    @GetMapping("/user/{userId}")
    public List<VocabList> getListsByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer exclude
    ) {
        List<VocabList> lists = vocabListService.getListsByUser(userId);
        if (exclude != null) {
            return lists.stream()
                    .filter(list -> !list.getListId().equals(exclude))
                    .collect(Collectors.toList());
        }
        return lists;
    }

    // ✅ Create new list
    @PostMapping
    public ResponseEntity<?> createList(@RequestBody VocabListRequest request) {
        try {
            VocabList created = vocabListService.createList(request.getUserId(), request.getListName());
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

    // 🔹 NEW: GET the “history” list id (first list by ASC) for a user
    // Returns 200 { "listId": number } or 404 if user has no lists
    @GetMapping("/user/{userId}/vocab-history")
    public ResponseEntity<Map<String, Integer>> getVocabHistory(@PathVariable Integer userId) {
        return vocabListService.findUserHistoryListId(userId)
                .map(id -> ResponseEntity.ok(Map.of("listId", id)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 NEW (optional): create “History” if missing and return its id
    @PostMapping("/user/{userId}/vocab-history")
    public ResponseEntity<Map<String, Integer>> createVocabHistoryIfMissing(@PathVariable Integer userId) {
        Integer id = vocabListService.getOrCreateHistoryListId(userId);
        return ResponseEntity.ok(Map.of("listId", id));
    }

    // Simple DTO to handle request body
    public static class VocabListRequest {
        private Integer userId;
        private String listName;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getListName() { return listName; }
        public void setListName(String listName) { this.listName = listName; }
    }
}
