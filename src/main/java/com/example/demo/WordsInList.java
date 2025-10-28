package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(
        name = "words_in_list",
        uniqueConstraints = @UniqueConstraint(columnNames = {"list_id", "word"})
)
public class WordsInList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Integer wordId; // Primary key

    // Many words belong to one user
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnore // prevent lazy proxy from being serialized
    private User user;

    // Many words belong to one vocab list
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", referencedColumnName = "listId", nullable = false)
    @JsonIgnore // prevent lazy proxy from being serialized
    private VocabList list;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String word;

    @NotBlank
    @Column(nullable = false, length = 255) // keep as-is; switch to TEXT if you need longer defs
    private String definition;

    public WordsInList() {}

    // ---------- JSON-friendly ID exposers (so the frontend still gets userId/listId) ----------
    @JsonProperty("userId")
    public Integer getUserId() {
        return user != null ? user.getUserId() : null;
    }

    @JsonProperty("listId")
    public Integer getListId() {
        return list != null ? list.getListId() : null;
    }

    // ---------- getters & setters ----------
    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VocabList getList() {
        return list;
    }

    public void setList(VocabList list) {
        this.list = list;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
