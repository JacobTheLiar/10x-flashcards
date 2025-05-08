# Kroki tworzenia oprogramowania

### legenda

🔨 - IDE tool
🤖 - AI Assist
☑️ - Zrobione



### kroki

1. Przygotowanie wysoko poziomowego opisu [high-leveldescription.md]; ☑️🤖
2. Specyfikacja techniczna [tech-stack.md]; ☑️🤖
3. Dokument wymagań produktu [prd.md]: ☑️🤖
   1. sesja planistyczna (3 - rundy);
   2. podsumowanie;
   3. wygenerowanie dokumentu;
4. Plan bazy danych (db-plan.md): ☑️🤖
   1. sesja planistyczna;
   2. podsumowanie;
   3. dokument;
5. Wygenerowanie pliku migracji bazy danych dla flyway [V1.0__create_base_schema.sql];☑️🤖
   1. Dostosowanie BE;
   2. Migracja do najnowszej wersji;
6. Wygenerowanie Encji na podstawie bazy danych [back-end]; ☑️🔨
7. Wygenerowanie Repozytoriów i dostosowanie konfiguracji [back-end];☑️🤖
8. API Endpoints: ☑️🤖
   1. sesja planistyczna
   2. dokument
9. Implementacja API: ☑️🤖
10. Implementacja Docker File z Ollama: ☑️🤖
    1. Dostosowanie ustawień do pełnego uruchomienia w środowisku Docker: ☑️🔨
11. Uruchomienie projektu front-end: ☑️🔨
    1. zmiana package manager: ☑️🔨
    2. dodanie zależności do generowania klas na podstawie API: ☑️🔨
    3. wygenerowanie klas powiązanych z API: ☑️🔨
12. Sesja planistyczna UI: ☑️🤖
    1. sesja planistyczna
    2. ui-plan.md