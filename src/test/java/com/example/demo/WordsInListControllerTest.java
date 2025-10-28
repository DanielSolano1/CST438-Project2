package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WordsInListControllerTest {

  private MockMvc mockMvc;

  @Mock
  private WordsInListService wordsInListService;

  @InjectMocks
  private WordsInListController wordsInListController;

  @BeforeEach
  void setup() {
      mockMvc = MockMvcBuilders.standaloneSetup(wordsInListController).build();
  }

  // Helper method to build test data
  private WordsInList createWord(Integer id, Integer userId, Integer listId, String word, String definition) {
    User user = new User();
    user.setUserId(userId);

    VocabList list = new VocabList();
    list.setListId(listId);

    WordsInList w = new WordsInList();
    w.setWordId(id);
    w.setUser(user);
    w.setList(list);
    w.setWord(word);
    w.setDefinition(definition);
    return w;
  }

  //GET /api/words/list/{listId}
  @Test
  void testGetWordsByList() throws Exception {
    WordsInList w1 = createWord(1, 1, 10, "apple", "a fruit");
    WordsInList w2 = createWord(2, 1, 10, "dog", "an animal");

    when(wordsInListService.getWordsByList(10)).thenReturn(List.of(w1, w2));

    mockMvc.perform(get("/api/words/list/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].word").value("apple"))
            .andExpect(jsonPath("$[1].word").value("dog"));
  }

  //POST /api/words
  @Test
  void testAddWordToList() throws Exception {
    WordsInList entry = createWord(3, 1, 10, "banana", "a yellow fruit");

    when(wordsInListService.addWordToList(eq(1), eq(10), eq("banana"), eq("a yellow fruit")))
            .thenReturn(entry);

    mockMvc.perform(post("/api/words")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "userId":1,
                            "listId":10,
                            "word":"banana",
                            "definition":"a yellow fruit"
                        }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.word").value("banana"))
            .andExpect(jsonPath("$.definition").value("a yellow fruit"));
  }

  //POST /api/words/list/{listId}
  @Test
  void testAddWordToListPathParam() throws Exception {
    WordsInList entry = createWord(4, 101, 10, "cat", "a small animal");

    when(wordsInListService.addWordToList(eq(1), eq(10), eq("cat"), eq("a small animal")))
            .thenReturn(entry);

    mockMvc.perform(post("/api/words/list/10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "userId":1,
                            "word":"cat",
                            "definition":"a small animal"
                        }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.word").value("cat"))
            .andExpect(jsonPath("$.definition").value("a small animal"));
  }

  // GET /api/words
  @Test
  void testGetAllWords() throws Exception {
    WordsInList w1 = createWord(1, 1, 10, "car", "a vehicle");
    WordsInList w2 = createWord(2, 1, 11, "tree", "a plant");

    when(wordsInListService.getAllWords()).thenReturn(List.of(w1, w2));

    mockMvc.perform(get("/api/words"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].word").value("car"))
            .andExpect(jsonPath("$[1].word").value("tree"));
  }

  //GET /api/words/user/{userId}
  @Test
  void testGetWordsByUser() throws Exception {
    WordsInList w = createWord(1, 1, 10, "sky", "the atmosphere");

    when(wordsInListService.getWordsByUser(1)).thenReturn(List.of(w));

    mockMvc.perform(get("/api/words/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].word").value("sky"))
            .andExpect(jsonPath("$[0].definition").value("the atmosphere"));
  }

  //DELETE /api/words/{wordId}
  @Test
  void testDeleteWord() throws Exception {
    mockMvc.perform(delete("/api/words/5"))
            .andExpect(status().isNoContent());

    verify(wordsInListService).deleteWord(5);
  }

  // Negative case: duplicate word
  @Test
  void testAddWordToList_DuplicateWord() throws Exception {
    when(wordsInListService.addWordToList(eq(1), eq(10), eq("apple"), eq("fruit")))
            .thenThrow(new IllegalArgumentException("Word already exists in list"));

    mockMvc.perform(post("/api/words")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "userId":1,
                            "listId":10,
                            "word":"apple",
                            "definition":"fruit"
                        }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Word already exists in list"));
  }
}
