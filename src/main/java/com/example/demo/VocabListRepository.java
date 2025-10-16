package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VocabListRepository extends JpaRepository<VocabList, Integer> {
    List<VocabList> findByUser_UserId(Integer userId);
}
