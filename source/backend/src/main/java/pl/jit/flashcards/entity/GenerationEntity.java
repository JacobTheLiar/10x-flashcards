package pl.jit.flashcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "generations")
public class GenerationEntity {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ColumnDefault("(now() AT TIME ZONE 'utc'::text)")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Size(max = 32)
    @NotNull
    @Column(name = "source_text_md5", nullable = false, length = 32)
    private String sourceTextMd5;

    @NotNull
    @Column(name = "source_text_length", nullable = false)
    private Integer sourceTextLength;

    @NotNull
    @Column(name = "generation_time_ms", nullable = false)
    private Integer generationTimeMs;

    @NotNull
    @Column(name = "suggested_flashcards_count", nullable = false)
    private Integer suggestedFlashcardsCount;

}