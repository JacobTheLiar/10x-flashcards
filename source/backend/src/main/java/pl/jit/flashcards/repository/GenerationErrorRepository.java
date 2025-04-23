package pl.jit.flashcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jit.flashcards.entity.GenerationErrorEntity;

import java.util.UUID;

@Repository
public interface GenerationErrorRepository extends JpaRepository<GenerationErrorEntity, UUID> {
    boolean existsByGenerations_Id(UUID generationId);
} 