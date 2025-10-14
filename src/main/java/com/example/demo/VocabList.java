package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "vocabulary_list"
)
public class VocabList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer listId;

    // Foreign key to the User table
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String listName;

    public VocabList() {}

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}
