package pl.jit.flashcards.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.request.CreateFlashcardRequest;
import pl.jit.flashcards.entity.FlashcardEntity;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlashcardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "generation", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(source = "sourceType", target = "sourceType")
    FlashcardEntity fromRequestToEntity(CreateFlashcardRequest requestDto);

    @Mapping(source = "sourceType", target = "sourceType")
    FlashcardApiModel toApiModel(FlashcardEntity entity);

    List<FlashcardApiModel> toApiModelList(List<FlashcardEntity> entities);
}