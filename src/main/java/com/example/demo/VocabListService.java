package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VocabListService {

    private final VocabListRepository vocabListRepository;
    private final UserRepository userRepository;

    public VocabListService(VocabListRepository vocabListRepository, UserRepository userRepository) {
        this.vocabListRepository = vocabListRepository;
        this.userRepository = userRepository;
    }

    public List<VocabList> getAllLists() {
        return vocabListRepository.findAll();
    }

    public List<VocabList> getListsByUser(Integer userId) {
        return vocabListRepository.findByUser_UserId(userId);
    }

    public Optional<VocabList> getListById(Integer id) {
        return vocabListRepository.findById(id);
    }

    public VocabList createList(Integer userId, String listName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VocabList list = new VocabList();
        list.setUser(user);
        list.setListName(listName);

        return vocabListRepository.save(list);
    }

    public void deleteList(Integer id) {
        vocabListRepository.deleteById(id);
    }
    // NEW: first listId for user (history analogue)
    public Optional<Integer> findUserHistoryListId(Integer userId) {
        return vocabListRepository
                .findFirstByUser_UserIdOrderByListIdAsc(userId)
                .map(VocabList::getListId);
    }

    // NEW (optional): create a "History" list if none exists, and return its id
    public Integer getOrCreateHistoryListId(Integer userId) {
        return findUserHistoryListId(userId).orElseGet(() -> {
            VocabList created = createList(userId, "History");
            return created.getListId();
        });
    }

    public boolean userOwnsList(Integer userId, Integer listId) {
        return vocabListRepository.existsByListIdAndUser_UserId(listId, userId);
    }
}
