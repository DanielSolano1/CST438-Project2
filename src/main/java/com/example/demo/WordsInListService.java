package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WordsInListService {

    private final WordsInListRepository wordsInListRepository;
    private final UserRepository userRepository;
    private final VocabListRepository vocabListRepository;

    public WordsInListService(
            WordsInListRepository wordsInListRepository,
            UserRepository userRepository,
            VocabListRepository vocabListRepository) {
        this.wordsInListRepository = wordsInListRepository;
        this.userRepository = userRepository;
        this.vocabListRepository = vocabListRepository;
    }

    public List<WordsInList> getAllWords() {
        return wordsInListRepository.findAll();
    }

    public List<WordsInList> getWordsByUser(Integer userId) {
        return wordsInListRepository.findByUser_UserId(userId);
    }

    public List<WordsInList> getWordsByList(Integer listId) {
        return wordsInListRepository.findByList_ListId(listId);
    }

    public WordsInList addWordToList(Integer userId, Integer listId, String word, String definition) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VocabList list = vocabListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        List<WordsInList> existing = wordsInListRepository.findByList_ListId(listId);
        boolean duplicate = existing.stream().anyMatch(w -> w.getWord().equalsIgnoreCase(word));
        if (duplicate) {
            throw new IllegalArgumentException("Word already exists in this list");
        }

        WordsInList entry = new WordsInList();
        entry.setUser(user);
        entry.setList(list);
        entry.setWord(word);
        entry.setDefinition(definition);

        return wordsInListRepository.save(entry);
    }

    public void deleteWord(Integer wordId) {
        wordsInListRepository.deleteById(wordId);
    }
}
