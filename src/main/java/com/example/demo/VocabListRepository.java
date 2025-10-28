package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VocabListRepository extends JpaRepository<VocabList, Integer> {
    List<VocabList> findByUser_UserId(Integer userId);
    // NEW: first list for a user by listId ASC
    Optional <VocabList> findFirstByUser_UserIdOrderByListIdAsc(Integer userId);

    // NEW: handy ownership check (used later)
    boolean existsByListIdAndUser_UserId(Integer listId, Integer userId);
}
