-- =============================================
-- Migration: V1.0__create_base_schema.sql
-- 
-- Autor: System / AI Assistant
-- Data: 2023-10-05 / 2025-05-05
-- 
-- Opis: Skonsolidowana inicjalizacja schematu bazy danych dla aplikacji 10x Flashcards.
--       - Tworzenie tabel: users, generations, generation_errors, flashcards.
--       - Dodanie ograniczeń CHECK i indeksów.
--       - Ustawienie domyślnej wartości user_id (z V1.1).
--       - Nadanie uprawnień użytkownikowi aplikacji.
--       - Implementacja Row Level Security (RLS).
-- =============================================

-- Rozpoczęcie transakcji
begin;

-- =============================================
-- Funkcje Pomocnicze (muszą być przed użyciem)
-- =============================================

-- Funkcja pomocnicza do identyfikacji bieżącego użytkownika dla RLS i wartości domyślnych
create or replace function current_user_id() returns uuid as
$$
begin
    return current_setting('app.current_user_id', true)::uuid;
exception
    when others then
        return null;
end;
$$ language plpgsql security definer;

DO
$do$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_catalog.pg_roles
            WHERE  rolname = '${app_username}') THEN

            CREATE ROLE ${app_username} LOGIN PASSWORD '${app_password}';
        END IF;
    END
$do$;

-- =============================================
-- Tworzenie tabel podstawowych
-- =============================================

-- Tabela users - przechowuje dane użytkowników
create table users (
   id uuid primary key default gen_random_uuid(),
   email varchar(255) not null unique,
   password_hash varchar(255) not null,
   created_at timestamp not null default (now() at time zone 'utc'),
   last_login_at timestamp
);

-- Tabela generations - przechowuje informacje o generacjach fiszek
create table generations (
   id uuid primary key default gen_random_uuid(),
   user_id uuid not null,
   created_at timestamp not null default (now() at time zone 'utc'),
   source_text_md5 varchar(32) not null,
   source_text_length integer not null,
   generation_time_ms integer not null,
   suggested_flashcards_count integer not null,
   constraint fk_generations_user
      foreign key (user_id)
      references users(id)
      on delete cascade
);

-- Tabela generation_errors - przechowuje błędy generacji
create table generation_errors (
   generation_id uuid primary key,
   error_code varchar(50) not null,
   error_message varchar(250) not null,
   constraint fk_generation_errors_generation
      foreign key (generation_id)
      references generations(id)
      on delete cascade
);

-- Tabela flashcards - przechowuje fiszki użytkowników
create table flashcards (
   id uuid primary key default gen_random_uuid(),
   user_id uuid not null,
   generation_id uuid,
   front_content varchar(500) not null,
   back_content varchar(200) not null,
   source_type varchar(20) not null
       constraint chk_flashcards_source_type check (source_type in ('ai-full', 'ai-edited', 'manual')),
   last_modified_at timestamp not null default (now() at time zone 'utc'),
   constraint fk_flashcards_user
      foreign key (user_id)
      references users(id)
      on delete cascade,
   constraint fk_flashcards_generation
      foreign key (generation_id)
      references generations(id)
      on delete set null
);

-- =============================================
-- Dodatkowe ograniczenia CHECK
-- =============================================

-- Ograniczenie długości treści fiszek
alter table flashcards
   add constraint chk_front_content_length
      check (length(front_content) <= 500),
   add constraint chk_back_content_length
      check (length(back_content) <= 200);

-- Ograniczenie długości wiadomości błędu
alter table generation_errors
   add constraint chk_error_message_length
      check (length(error_message) <= 250);

-- =============================================
-- Tworzenie indeksów
-- =============================================

-- Indeks dla wyszukiwania fiszek użytkownika
create index idx_flashcards_user_id on flashcards(user_id);

-- Indeks dla relacji fiszek z generacją
create index idx_flashcards_generation_id on flashcards(generation_id);

-- Indeks dla wyszukiwania generacji użytkownika
create index idx_generations_user_id on generations(user_id);

-- =============================================
-- Ustawienie wartości domyślnych (z V1.1)
-- =============================================

-- Ustawienie wartości domyślnej dla user_id w tabeli generations
alter table generations
    alter column user_id set default current_user_id();

-- Ustawienie wartości domyślnej dla user_id w tabeli flashcards
alter table flashcards
    alter column user_id set default current_user_id();

-- =============================================
-- Nadanie uprawnień użytkownikowi aplikacji
-- =============================================

GRANT SELECT, INSERT, UPDATE, DELETE ON users TO ${app_username};
GRANT SELECT, INSERT, UPDATE, DELETE ON generations TO ${app_username};
GRANT SELECT, INSERT, UPDATE, DELETE ON generation_errors TO ${app_username};
GRANT SELECT, INSERT, UPDATE, DELETE ON flashcards TO ${app_username};

-- =============================================
-- Implementacja Row Level Security (RLS)
-- =============================================

-- Włączenie RLS dla tabeli flashcards
alter table flashcards enable row level security;

-- Włączenie RLS dla tabeli generations
alter table generations enable row level security;

-- Włączenie RLS dla tabeli generation_errors
alter table generation_errors enable row level security;

-- Polityka dla tabeli flashcards - użytkownicy mogą przeglądać tylko swoje fiszki
create policy flashcards_user_select on flashcards
    for select
    using (user_id = current_user_id());

-- Polityka dla tabeli flashcards - użytkownicy mogą modyfikować tylko swoje fiszki
create policy flashcards_user_modify on flashcards
    for all
    using (user_id = current_user_id());

-- Polityka dla tabeli generations - użytkownicy mogą przeglądać tylko swoje generacje
create policy generations_user_select on generations
    for select
    using (user_id = current_user_id());

-- Polityka dla tabeli generations - użytkownicy mogą modyfikować tylko swoje generacje
create policy generations_user_modify on generations
    for all
    using (user_id = current_user_id());

-- Polityka dla tabeli generation_errors - dostęp powiązany z właścicielem generacji
create policy generation_errors_user_select on generation_errors
    for select
    using (generation_id in (select id from generations where user_id = current_user_id()));

-- Polityka dla tabeli generation_errors - modyfikacje powiązane z właścicielem generacji
create policy generation_errors_user_modify on generation_errors
    for all
    using (generation_id in (select id from generations where user_id = current_user_id()));

-- Zatwierdzenie transakcji
commit;
