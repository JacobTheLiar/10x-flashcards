# Paths

## backend
- source path: `./source/backend/src/main/java`
- test path: `./source/backend/src/test/java`
- migration path: `.source/backend/src/main/resources/db/migration`
- entity package path: `./source/backend/src/main/java/pl/jit/flashcards/entity`
- repository package path: `./source/backend/src/main/java/pl/jit/flashcards/repository`
- Data Transfer Objects: `./source/backend/src/main/java/pl/jit/flashcards/data`
- DTO Models: `./source/backend/src/main/java/pl/jit/flashcards/data/api_model`
- DTO Requests: `./source/backend/src/main/java/pl/jit/flashcards/data/request`
- DTO Responses: `./source/backend/src/main/java/pl/jit/flashcards/data/response`

# Naming convention

Always one file per one java class.

## backend

- Entity ClassName always with suffix Entity, e.g., UserEntity;
- Repository Classname always with suffix Repository e.g., UserRepository
- For Data Transfer Object used in API:
  - general suffix ApiModel e.g. UserApiModel
  - for api requests suffix Request e.g. CreateUserRequest
  - for api responses suffix Response e.g. CreateUserResponse