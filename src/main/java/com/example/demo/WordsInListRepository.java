package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WordsInListRepository extends JpaRepository<WordsInList, Integer> {
    List<WordsInList> findByList_ListId(Integer listId);
    List<WordsInList> findByUser_UserId(Integer userId);
    @Override
    Optional<WordsInList> findById(Integer integer);

}

