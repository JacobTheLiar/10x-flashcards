package pl.jit.flashcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jit.flashcards.entity.GenerationEntity;

import java.util.UUID;

@Repository
public interface GenerationRepository extends JpaRepository<GenerationEntity, UUID> {
    Page<GenerationEntity> findAll(Pageable pageable);
    
    long countBySuggestedFlashcardsCount(int suggestedFlashcardsCount);
} 