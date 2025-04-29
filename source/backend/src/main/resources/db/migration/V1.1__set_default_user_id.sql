-- =============================================
-- Migration: V1.1__set_default_user_id.sql
--
-- Autor: AI Assistant
-- Data: 2025-04-29
--
-- Opis: Ustawienie domyślnej wartości dla kolumny user_id w tabelach
--       generations i flashcards przy użyciu funkcji current_user_id().
--       Upraszcza to wstawianie nowych rekordów, ponieważ aplikacja
--       nie musi już jawnie podawać user_id, jeśli ustawienie
--       'app.current_user_id' jest dostępne w sesji.
-- =============================================

-- Rozpoczęcie transakcji
begin;

-- Ustawienie wartości domyślnej dla user_id w tabeli generations
alter table generations
    alter column user_id set default current_user_id();

-- Ustawienie wartości domyślnej dla user_id w tabeli flashcards
alter table flashcards
    alter column user_id set default current_user_id();

-- Zatwierdzenie transakcji
commit;