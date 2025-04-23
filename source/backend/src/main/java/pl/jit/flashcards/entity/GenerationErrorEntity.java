package pl.jit.flashcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "generation_errors")
public class GenerationErrorEntity {
    @Id
    @Column(name = "generation_id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "generation_id", nullable = false)
    private GenerationEntity generations;

    @Size(max = 50)
    @NotNull
    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "error_message", nullable = false, length = 250)
    private String errorMessage;

}