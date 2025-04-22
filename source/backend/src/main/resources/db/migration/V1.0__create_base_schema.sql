-- =============================================
-- Migration: V1.0__create_base_schema.sql
-- 
-- Autor: System
-- Data: 2023-10-05
-- 
-- Opis: Inicjalizacja schematu bazy danych dla aplikacji 10x Flashcards.
-- Tabele: users, generations, generation_errors, flashcards.
-- Implementacja Row Level Security (RLS) dla dostępu do danych.
-- =============================================

-- Rozpoczęcie transakcji
begin;

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
   source_type varchar(20) not null check (source_type in ('ai-full', 'ai-modified', 'manual')),
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
-- Implementacja Row Level Security (RLS)
-- =============================================

-- Funkcja pomocnicza do identyfikacji bieżącego użytkownika
create or replace function current_user_id() returns uuid as $$
begin
    -- Zwraca ID użytkownika z kontekstu sesji
    -- Wartość ta jest ustawiana przez aplikację przy logowaniu
    return current_setting('app.current_user_id', true)::uuid;
exception
    when others then
        return null;
end;
$$ language plpgsql security definer;

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
