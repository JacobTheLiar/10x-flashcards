-- =============================================
-- Migration: V1.2__update_flashcard_source_type_check.sql
--
-- Autor: AI Assistant
-- Data: 2025-05-02
--
-- Opis: Aktualizuje ograniczenie CHECK dla kolumny source_type
--       w tabeli flashcards, aby zawierało poprawną wartość 'ai-edited'
--       zamiast 'ai-modified', zgodnie z implementacją w kodzie Java.
-- =============================================

-- Rozpoczęcie transakcji
begin;

-- Usunięcie starego ograniczenia CHECK.
-- Nazwa ograniczenia mogła zostać wygenerowana automatycznie przez PostgreSQL,
-- jeśli nie została jawnie podana w V1.0. Standardowa nazwa to często '{table}_{column}_check'.
-- Jeśli poniższe polecenie zawiedzie, sprawdź dokładną nazwę ograniczenia w swojej bazie danych, np. używając:
-- SELECT conname FROM pg_constraint WHERE conrelid = 'flashcards'::regclass AND contype = 'c' AND conname LIKE '%source_type%';
-- Zakładamy tutaj standardową nazwę 'flashcards_source_type_check'.
-- W V1.0 constraint został dodany wewnątrz CREATE TABLE, bez jawnej nazwy, więc to jest prawdopodobne.
-- Jeśli V1.0 faktycznie nadał mu inną nazwę, użyj tej nazwy poniżej.
-- Z kodu V1.0 wynika, że nie nadano mu jawnie nazwy.
ALTER TABLE flashcards DROP CONSTRAINT flashcards_source_type_check;

-- Dodanie nowego, poprawnego ograniczenia CHECK z jawną nazwą
ALTER TABLE flashcards
    ADD CONSTRAINT chk_flashcards_source_type
        CHECK (source_type IN ('ai-full', 'ai-edited', 'manual'));

-- Komunikat logowania (opcjonalny, ale pomocny)
-- W PostgreSQL można użyć RAISE NOTICE, ale to może nie być standardem Flyway.
-- Zamiast tego po prostu dodajemy komentarz.
-- Poprawiono ograniczenie CHECK dla flashcards.source_type.

-- Zatwierdzenie transakcji
commit;
