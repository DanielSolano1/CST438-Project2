package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VocabListControllerTest {

  private MockMvc mockMvc;

  @Mock
  private VocabListService vocabListService;

  @InjectMocks
  private VocabListController vocabListController;

  @BeforeEach
  void setup() {
    // Build MockMvc manually with your controller
    mockMvc = MockMvcBuilders.standaloneSetup(vocabListController).build();
  }

  private VocabList createList(Integer id, String name, Integer userId) {
    User user = new User();
    user.setUserId(userId);

    VocabList list = new VocabList();
    list.setListId(id);
    list.setListName(name);
    list.setUser(user);
    return list;
  }

  @Test
  void testGetListsByUser() throws Exception {
    VocabList list1 = createList(1, "Animals", 1);
    VocabList list2 = createList(2, "Food", 1);

    Mockito.when(vocabListService.getListsByUser(1))
            .thenReturn(List.of(list1, list2));

    mockMvc.perform(get("/api/lists/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].listName").value("Animals"))
            .andExpect(jsonPath("$[1].listName").value("Food"));
  }

  @Test
  void testCreateList() throws Exception {
    VocabList created = createList(10, "New Words", 1);

    Mockito.when(vocabListService.createList(eq(1), eq("New Words")))
            .thenReturn(created);

    mockMvc.perform(post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userId\":1, \"listName\":\"New Words\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listId").value(10))
            .andExpect(jsonPath("$.listName").value("New Words"));
  }

  @Test
  void testGetVocabHistoryFound() throws Exception {
    Mockito.when(vocabListService.findUserHistoryListId(1))
            .thenReturn(Optional.of(5));

    mockMvc.perform(get("/api/lists/user/1/vocab-history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listId").value(5));
  }

  @Test
  void testGetVocabHistoryNotFound() throws Exception {
    Mockito.when(vocabListService.findUserHistoryListId(1))
            .thenReturn(Optional.empty());

    mockMvc.perform(get("/api/lists/user/1/vocab-history"))
            .andExpect(status().isNotFound());
  }

  @Test
  void testCreateVocabHistoryIfMissing() throws Exception {
    Mockito.when(vocabListService.getOrCreateHistoryListId(1))
            .thenReturn(42);

    mockMvc.perform(post("/api/lists/user/1/vocab-history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.listId").value(42));
  }

  @Test
  void testDeleteList() throws Exception {
    mockMvc.perform(delete("/api/lists/5"))
            .andExpect(status().isNoContent());
    Mockito.verify(vocabListService).deleteList(5);
  }
}