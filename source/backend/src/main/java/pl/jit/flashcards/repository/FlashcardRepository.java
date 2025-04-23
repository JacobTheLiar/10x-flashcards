package pl.jit.flashcards.repository;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.jit.flashcards.entity.FlashcardEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardRepository extends JpaRepository<FlashcardEntity, UUID> {
    
    @Query(value = "SELECT f FROM FlashcardEntity f ORDER BY RANDOM() LIMIT :limit")
    List<FlashcardEntity> findRandomFlashcards(int limit);
    
    Page<FlashcardEntity> findAll(@NonNull Pageable pageable);
    
    List<FlashcardEntity> findByGeneration_Id(UUID generationId);
    
    @Query("SELECT COUNT(f) FROM FlashcardEntity f WHERE f.sourceType = :sourceType")
    long countBySourceType(String sourceType);
} 