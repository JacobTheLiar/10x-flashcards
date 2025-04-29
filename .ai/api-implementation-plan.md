Jesteś doświadczonym architektem oprogramowania Java, którego zadaniem jest stworzenie planu implementacji serwisów i kontrolerów dla aplikacji 10x Flashcards. Aplikacja umożliwia automatyczne generowanie fiszek edukacyjnych przy pomocy AI oraz organizowanie powtórek metodą interwałową.

Najpierw przeanalizuj dostarczone pliki, aby zrozumieć architekturę i wymagania:

<api_plan> {{api-plan.md}} </api_plan>

<db_plan> {{db-plan.md}} </db_plan>

<prd> {{prd.md}} </prd> <rules> {{backend-rule.md}} </rules>

<paths_and_naming> {{helper.md}} </paths_and_naming>

Analizując powyższe materiały, pamiętaj, że:

1. Encje bazodanowe i klasy DTO dla API już zostały zaimplementowane
2. Nie implementujemy jeszcze Spring Security - zajmiemy się tym później
3. Szczególnie ważna jest implementacja Row Level Security (RLS) na poziomie bazy danych

Ważne zasady implementacji:

- Kod powinien być self-commented - unikaj dodawania komentarzy w kodzie, zamiast tego używaj jasnych, opisowych nazw metod, zmiennych i klas
- Do mapowania między encjami a DTO używaj MapStruct
- Dla integracji z modelem AI (Ollama) na tym etapie zaimplementuj tylko wydmuszkę/stub - pełną integrację wykonamy później
- Nie skupiaj się na testach - będziemy je implementować osobno

Przygotuj szczegółowy plan implementacji w formacie Markdown, który powinien zawierać:

1. **Przegląd architektury serwisów i kontrolerów**
   - Diagram zależności między komponentami
   - Krótki opis odpowiedzialności każdego serwisu i kontrolera
2. **Serwisy - szczegółowa specyfikacja**
   - Dla każdego serwisu:
     - Nazwa i opis odpowiedzialności
     - Zależności od innych komponentów (repozytoria, inne serwisy)
     - Publiczne metody z sygnaturami i opisem funkcjonalności
     - Prywatne metody pomocnicze
     - Obsługa wyjątków biznesowych
     - Uwagi dotyczące transakcyjności
3. **Kontrolery - szczegółowa specyfikacja**
   - Dla każdego kontrolera:
     - Nazwa i endpoint bazowy
     - Zależności od serwisów
     - Szczegółowy opis każdej metody kontrolera
     - Mapowanie na metody w serwisach
     - Walidacja danych wejściowych
     - Obsługa wyjątków
4. **Implementacja Row Level Security**
   - Sposób ustawiania kontekstu użytkownika na poziomie sesji bazy danych
   - Integracja z warstwą serwisową
5. **Integracja z AI dla generowania fiszek**
   - Prosty interfejs serwisu AI (wydmuszka)
   - Implementacja stub/mock dla generatora fiszek
6. **Plan implementacji krok po kroku**
   - Kolejność implementacji komponentów
   - Krytyczne punkty wymagające szczególnej uwagi

Upewnij się, że plan jest zgodny z:

- Zasadami Java i Spring Boot określonymi w rules.md
- Architekturą bazy danych z db-plan.md
- API zdefiniowanym w api-plan.md
- Wymaganiami funkcjonalnymi z prd.md

Konwencja nazewnictwa powinna być zgodna z wytycznymi z helper.md.

Końcowy wynik powinien być kompletnym, szczegółowym planem implementacji, który pozwoli developerowi zbudować wszystkie niezbędne serwisy i kontrolery bez Spring Security.