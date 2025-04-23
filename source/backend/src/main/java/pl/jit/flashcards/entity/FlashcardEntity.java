package pl.jit.flashcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "flashcards")
public class FlashcardEntity {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "generation_id")
    private GenerationEntity generation;

    @Size(max = 500)
    @NotNull
    @Column(name = "front_content", nullable = false, length = 500)
    private String frontContent;

    @Size(max = 200)
    @NotNull
    @Column(name = "back_content", nullable = false, length = 200)
    private String backContent;

    @Size(max = 20)
    @NotNull
    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @NotNull
    @ColumnDefault("(now() AT TIME ZONE 'utc'::text)")
    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

}