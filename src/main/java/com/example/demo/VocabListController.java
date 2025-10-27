package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
