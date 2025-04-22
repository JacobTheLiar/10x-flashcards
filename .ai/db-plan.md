# Schemat bazy danych PostgreSQL dla 10x Flashcards

## 1. Lista tabel z ich kolumnami, typami danych i ograniczeniami

### 1.1. Tabela `users`

| Kolumna | Typ danych | Ograniczenia |
|---------|------------|--------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, DEFAULT (NOW() AT TIME ZONE 'UTC') |
| last_login_at | TIMESTAMP | NULL |

```sql
CREATE TABLE users (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   email VARCHAR(255) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
   last_login_at TIMESTAMP
);
```

### 1.2. Tabela `generations`

| Kolumna | Typ danych | Ograniczenia |
|---------|------------|--------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() |
| user_id | UUID | NOT NULL, FOREIGN KEY |
| created_at | TIMESTAMP | NOT NULL, DEFAULT (NOW() AT TIME ZONE 'UTC') |
| source_text_md5 | VARCHAR(32) | NOT NULL |
| source_text_length | INTEGER | NOT NULL |
| generation_time_ms | INTEGER | NOT NULL |
| suggested_flashcards_count | INTEGER | NOT NULL |

```sql
CREATE TABLE generations (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
   source_text_md5 VARCHAR(32) NOT NULL,
   source_text_length INTEGER NOT NULL,
   generation_time_ms INTEGER NOT NULL,
   suggested_flashcards_count INTEGER NOT NULL,
   CONSTRAINT fk_generations_user
      FOREIGN KEY (user_id)
      REFERENCES users(id)
      ON DELETE CASCADE
);
```

### 1.3. Tabela `generation_errors`

| Kolumna | Typ danych | Ograniczenia |
|---------|------------|--------------|
| generation_id | UUID | PRIMARY KEY, FOREIGN KEY |
| error_code | VARCHAR(50) | NOT NULL |
| error_message | VARCHAR(250) | NOT NULL |

```sql
CREATE TABLE generation_errors (
   generation_id UUID PRIMARY KEY,
   error_code VARCHAR(50) NOT NULL,
   error_message VARCHAR(250) NOT NULL,
   CONSTRAINT fk_generation_errors_generation
      FOREIGN KEY (generation_id)
      REFERENCES generations(id)
      ON DELETE CASCADE
);
```

### 1.4. Tabela `flashcards`

| Kolumna | Typ danych | Ograniczenia |
|---------|------------|--------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() |
| user_id | UUID | NOT NULL, FOREIGN KEY |
| generation_id | UUID | NULL, FOREIGN KEY |
| front_content | VARCHAR(500) | NOT NULL |
| back_content | VARCHAR(200) | NOT NULL |
| source_type | VARCHAR(20) | NOT NULL, CHECK (source_type IN ('ai-full', 'ai-modified', 'manual')) |
| last_modified_at | TIMESTAMP | NOT NULL, DEFAULT (NOW() AT TIME ZONE 'UTC') |

```sql
CREATE TABLE flashcards (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL,
   generation_id UUID,
   front_content VARCHAR(500) NOT NULL,
   back_content VARCHAR(200) NOT NULL,
   source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('ai-full', 'ai-modified', 'manual')),
   last_modified_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
   CONSTRAINT fk_flashcards_user
      FOREIGN KEY (user_id)
      REFERENCES users(id)
      ON DELETE CASCADE,
   CONSTRAINT fk_flashcards_generation
      FOREIGN KEY (generation_id)
      REFERENCES generations(id)
      ON DELETE SET NULL
);
```

## 2. Relacje między tabelami

| Tabela źródłowa | Kolumna | Tabela docelowa | Typ relacji | Akcja przy usunięciu |
|-----------------|---------|-----------------|-------------|----------------------|
| generations | user_id | users | wiele do jednego | CASCADE |
| generation_errors | generation_id | generations | jeden do jednego | CASCADE |
| flashcards | user_id | users | wiele do jednego | CASCADE |
| flashcards | generation_id | generations | wiele do jednego | SET NULL |


## 3. Indeksy

| Nazwa indeksu | Tabela | Kolumna(y) | Typ indeksu | Cel |
|---------------|--------|------------|-------------|-----|
| (PK) | users | id | PRIMARY KEY | Identyfikacja użytkownika |
| (UNIQUE) | users | email | UNIQUE | Unikalne loginy użytkowników |
| (PK) | generations | id | PRIMARY KEY | Identyfikacja procesu generacji |
| (PK) | generation_errors | generation_id | PRIMARY KEY | Identyfikacja błędu generacji |
| (PK) | flashcards | id | PRIMARY KEY | Identyfikacja fiszki |
| idx_flashcards_user_id | flashcards | user_id | INDEX | Szybkie wyszukiwanie fiszek użytkownika |
| idx_flashcards_generation_id | flashcards | generation_id | INDEX | Szybkie wyszukiwanie fiszek z danej generacji |
| idx_generations_user_id | generations | user_id | INDEX | Szybkie wyszukiwanie generacji użytkownika |

```sql
-- Indeks dla wyszukiwania fiszek użytkownika
CREATE INDEX idx_flashcards_user_id ON flashcards(user_id);

-- Indeks dla relacji fiszek z generacją
CREATE INDEX idx_flashcards_generation_id ON flashcards(generation_id);

-- Indeks dla wyszukiwania generacji użytkownika
CREATE INDEX idx_generations_user_id ON generations(user_id);
```

## 4. Zasady PostgreSQL

### 4.1. Ograniczenia CHECK

| Nazwa ograniczenia | Tabela | Warunek | Cel |
|--------------------|--------|---------|-----|
| source_type CHECK | flashcards | source_type IN ('ai-full', 'ai-modified', 'manual') | Ograniczenie dozwolonych wartości źródła fiszki |
| chk_front_content_length | flashcards | length(front_content) <= 500 | Ograniczenie długości treści przedniej strony |
| chk_back_content_length | flashcards | length(back_content) <= 200 | Ograniczenie długości treści tylnej strony |
| chk_error_message_length | generation_errors | length(error_message) <= 250 | Ograniczenie długości komunikatu błędu |

```sql
-- Ograniczenie długości treści fiszek
ALTER TABLE flashcards
   ADD CONSTRAINT chk_front_content_length
      CHECK (length(front_content) <= 500),
   ADD CONSTRAINT chk_back_content_length
      CHECK (length(back_content) <= 200);

-- Ograniczenie długości wiadomości błędu
ALTER TABLE generation_errors
   ADD CONSTRAINT chk_error_message_length
      CHECK (length(error_message) <= 250);
```

### 4.2. Row Level Security (RLS)

Row Level Security (RLS) jest kluczowym mechanizmem zapewniającym bezpieczeństwo danych na poziomie wierszy w tabelach. System 10x Flashcards wymaga, aby użytkownicy mieli dostęp tylko do własnych danych.

#### 4.2.1. Włączenie RLS dla tabel

```sql
-- Włączenie RLS dla tabeli flashcards
ALTER TABLE flashcards ENABLE ROW LEVEL SECURITY;

-- Włączenie RLS dla tabeli generations
ALTER TABLE generations ENABLE ROW LEVEL SECURITY;

-- Włączenie RLS dla tabeli generation_errors
ALTER TABLE generation_errors ENABLE ROW LEVEL SECURITY;
```

#### 4.2.2. Polityki dostępu

Polityki RLS definiują reguły dostępu do danych dla poszczególnych operacji (SELECT, INSERT, UPDATE, DELETE):

```sql
-- Polityka dla tabeli flashcards - użytkownicy mogą przeglądać tylko swoje fiszki
CREATE POLICY flashcards_user_select ON flashcards
    FOR SELECT
    USING (user_id = current_user_id());

-- Polityka dla tabeli flashcards - użytkownicy mogą modyfikować tylko swoje fiszki
CREATE POLICY flashcards_user_modify ON flashcards
    FOR ALL
    USING (user_id = current_user_id());

-- Polityka dla tabeli generations - użytkownicy mogą przeglądać tylko swoje generacje
CREATE POLICY generations_user_select ON generations
    FOR SELECT
    USING (user_id = current_user_id());

-- Polityka dla tabeli generation_errors - dostęp powiązany z właścicielem generacji
CREATE POLICY generation_errors_user_select ON generation_errors
    FOR SELECT
    USING (generation_id IN (SELECT id FROM generations WHERE user_id = current_user_id()));
```

#### 4.2.3. Funkcja current_user_id()

Funkcja pomocnicza do identyfikacji bieżącego użytkownika na podstawie kontekstu aplikacji:

```sql
CREATE OR REPLACE FUNCTION current_user_id() RETURNS UUID AS $
BEGIN
    -- Zwraca ID użytkownika z kontekstu sesji
    -- Wartość ta jest ustawiana przez aplikację przy logowaniu
    RETURN current_setting('app.current_user_id', TRUE)::UUID;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$ LANGUAGE plpgsql SECURITY DEFINER;
```

#### 4.2.4. Ustawienie kontekstu sesji

Kontekst sesji jest ustawiany przez aplikację przy każdym żądaniu:

```sql
-- Przykład ustawienia kontekstu użytkownika w aplikacji
SET app.current_user_id = '12345678-1234-1234-1234-123456789012';
```

### 4.3. Funkcje i wyzwalacze

Oprócz funkcji związanych z RLS, nie ma innych specjalnych funkcji i wyzwalaczy w obecnej wersji schematu.

## 5. Dodatkowe uwagi i wyjaśnienia decyzji projektowych

### 5.1. Wybór UUID zamiast sekwencyjnych identyfikatorów

Zastosowanie UUID jako kluczy głównych we wszystkich tabelach zapewnia:
- Unikalność identyfikatorów nawet w rozproszonych systemach
- Lepsze bezpieczeństwo przez utrudnienie enumeracji rekordów
- Łatwą migrację danych między środowiskami
- Możliwość tworzenia identyfikatorów bez dostępu do bazy danych
- Rozproszenie wpisów w indeksach, co może poprawić wydajność w dużych tabelach

### 5.2. Modelowanie błędów generacji

Tabela `generation_errors` została zaprojektowana jako relacja 1:1 z `generations`:
- Separacja błędów od normalnego przepływu danych
- Błędy generacji są przypadkami brzegowymi, więc nie obciążają głównej tabeli
- Możliwość rozszerzenia o dodatkowe informacje diagnostyczne w przyszłości

### 5.3. Pole source_text_md5 w generations

Przechowywanie hasha MD5 tekstu źródłowego zamiast pełnej treści:
- Oszczędność miejsca w bazie danych
- Możliwość wykrywania duplikatów wejść
- Zapewnienie prywatności danych przy zachowaniu możliwości analizy statystycznej
- Unikanie problemów wydajnościowych przy dużych tekstach źródłowych

### 5.4. Typy fiszek

Pole `source_type` z wartościami 'ai-full', 'ai-modified', 'manual':
- Umożliwia śledzenie pochodzenia fiszek
- Pozwala na analizę efektywności AI vs. treści tworzonych ręcznie
- Wspiera potrzeby biznesowe dotyczące metryk akceptacji propozycji AI

### 5.5. Kaskadowe usuwanie

- Usunięcie użytkownika powoduje usunięcie wszystkich jego fiszek i generacji
- Usunięcie generacji powoduje usunięcie powiązanych błędów
- Usunięcie generacji NIE usuwa fiszek, a jedynie ustawia generation_id na NULL

### 5.6. Implementacja Row Level Security (RLS)

Zastosowanie RLS w bazie danych jest kluczowym elementem bezpieczeństwa aplikacji:
- Zapewnia bezpieczeństwo na poziomie bazy danych, niezależnie od kodu aplikacji
- Eliminuje ryzyko ominięcia walidacji dostępu w aplikacji
- Zgodne z zasadą "defense in depth" - wielowarstwowe zabezpieczenia
- Umożliwia precyzyjne określenie dostępu do danych na poziomie wierszy
- Zapobiega atakom polegającym na manipulacji parametrami zapytań w celu uzyskania dostępu do cudzych danych

Architektura RLS:
- Polityki RLS definiują, które wiersze są widoczne dla danego użytkownika
- Kontekst użytkownika jest ustawiany na poziomie sesji bazy danych
- Funkcja `current_user_id()` zapewnia jednolity sposób identyfikacji bieżącego użytkownika
- Wszystkie tabele z danymi użytkownika (flashcards, generations, generation_errors) są chronione przez RLS

### 5.7. Potencjalne rozszerzenia schematu

W przyszłych wersjach można rozważyć:
1. Dodanie tabeli `flashcard_tags` do organizacji fiszek w zestawy
2. Rozszerzenie tabeli `flashcards` o informacje związane z algorytmem powtórek
3. Dodanie tabeli `user_preferences` do personalizacji doświadczeń
4. Implementację mechanizmu wersjonowania fiszek do śledzenia zmian
5. Rozszerzenie RLS o bardziej zaawansowane polityki dostępu (np. współdzielenie zestawów fiszek)