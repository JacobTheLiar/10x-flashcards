# Dokument wymagań produktu (PRD) - 10x Flashcards

## 1. Przegląd produktu
Produkt ma na celu ułatwienie nauki poprzez automatyczne generowanie fiszek edukacyjnych przy wykorzystaniu AI. Głównym problemem, który rozwiązujemy, jest trudność i czasochłonność manualnego tworzenia wysokiej jakości fiszek, co często zniechęca użytkowników. W MVP (Minimum Viable Product) skupiamy się na:
- Generowaniu fiszek przez AI na podstawie wklejonego tekstu (do 10 000 znaków) jako propozycji, która nie jest zapisywana w bazie danych.
- Przeglądaniu, edycji oraz usuwaniu fiszek w formie siatki (3-4 kolumn).
- Systemie powtórek, który umożliwia nawigację między fiszkami oraz zakończenie sesji, przekierowując użytkownika do ekranu generowania.

Interfejs użytkownika zostanie stworzony jako one-pager oparty o Angular oraz Angular Material, przeznaczony dla przeglądarek desktopowych. Aplikacja uruchomiona będzie w środowisku docker-compose z modelem AI (Ollama/Mistral 7B Instruct) oraz na infrastrukturze VPS (16GB RAM, 4 rdzenie, 50GB dysku).

## 2. Problem użytkownika
Głównym problemem jest to, że tworzenie fiszek ręcznie jest czasochłonne i pracochłonne. Użytkownicy, chcąc korzystać z metody spaced repetition, często rezygnują z ręcznego generowania fiszek, co wpływa negatywnie na efektywność nauki. Automatyzacja tego procesu przez inteligentne generowanie treści pozwoli:
- Zaoszczędzić czas użytkowników.
- Ułatwić proces nauki poprzez szybką i intuicyjną interakcję z systemem.
- Zwiększyć efektywność metody powtórek dzięki przemyślanemu doborowi fiszek.

## 3. Wymagania funkcjonalne
1. **Generowanie fiszek przez AI:**
    - Użytkownik wkleja tekst (maksymalnie 10 000 znaków).
    - System analizuje tekst i generuje sugerowane fiszki na podstawie zawartości, które służą wyłącznie jako propozycja.
    - Propozycje fiszek nie są zapisywane w bazie danych; dopiero zatwierdzenie przez użytkownika (przycisk „akceptuj”) powoduje ich zapis.
    - Fiszki wyświetlane są w formie siatki (3-4 kolumn) z opcjami „akceptuj”, „edytuj” oraz „odrzuć”.

2. **Przeglądanie fiszek:**
    - Użytkownik ma dostęp do widoku listy zaakceptowanych i zapisanych fiszek.
    - Funkcje edycji oraz usuwania są dostępne dla każdej zapisanej fiszki.

3. **System powtórek:**
    - Fiszki do powtórek są wybierane losowo (od 5 do 15 fiszek) przy użyciu prostego algorytmu losowego.
    - Użytkownik może przełączać się między fiszkami za pomocą przycisków „poprzedni” i „następny”.
    - Na ostatniej fiszce przycisk „następny” zmienia się na „zakończ”, przekierowując użytkownika do ekranu generowania.

4. **Interakcje użytkownika:**
    - Proces interakcji polega na decyzji, czy dana fiszka, będąca propozycją generowaną przez AI, zostanie zapisana w bazie danych.
    - Użytkownik klika przycisk „akceptuj” celem zapisania fiszki lub „odrzuć”, aby ją usunąć. Edycja fiszki przed zatwierdzeniem pozwala na wprowadzenie poprawek.
    - Decyzja użytkownika wpływa jedynie na zapis fiszki w bazie, nie modyfikując algorytmu powtórek.

5. **Uwierzytelnianie i autoryzacja:**
    - System kont użytkowników umożliwi bezpieczny dostęp do zapisanych fiszek oraz personalizację doświadczenia.
    - Użytkownik musi mieć możliwość rejestracji oraz logowania się do systemu, co zapewni bezpieczny dostęp do danych.

## 4. Granice produktu
- MVP nie obejmuje funkcjonalności manualnego tworzenia fiszek, aby nie zaciemniać modelu AI.
- Zaawansowany algorytm powtórek (np. podobny do SuperMemo lub Anki) nie zostanie wdrożony; wykorzystywany będzie prosty algorytm losowy.
- Aplikacja nie będzie wspierać importu wielu formatów (np. PDF, DOCX) – jedynie tekst kopiuj-wklej.
- Funkcjonalność współdzielenia zestawów fiszek między użytkownikami nie zostanie uwzględniona w MVP.
- Na początek aplikacja będzie dostępna tylko jako web i zoptymalizowana pod kątem przeglądarek desktopowych.

## 5. Historyjki użytkowników

### US-001
- **ID:** US-001
- **Tytuł:** Generowanie fiszek z wprowadzonego tekstu
- **Opis:** Użytkownik wkleja tekst do dedykowanego pola na głównym ekranie, a system na podstawie wprowadzonego tekstu generuje sugerowane fiszki przy użyciu algorytmu AI. Propozycje fiszek nie są zapisywane w bazie danych; zapis następuje dopiero po zatwierdzeniu.
- **Kryteria akceptacji:**
    - Użytkownik może wprowadzić tekst o długości do 10 000 znaków.
    - Po wklejeniu tekstu system wyświetla wygenerowane fiszki w formie siatki (3-4 kolumn).
    - Fiszki są prezentowane jako propozycje, które nie są zapisywane w bazie danych.
    - Proces zapisu fiszki następuje wyłącznie po zatwierdzeniu przez kliknięcie przycisku „akceptuj”.

### US-002
- **ID:** US-002
- **Tytuł:** Przeglądanie i zarządzanie fiszkami
- **Opis:** Użytkownik ma możliwość przeglądania zapisanych fiszek w widoku listy oraz ich edytowania lub usuwania.
- **Kryteria akceptacji:**
    - Użytkownik otrzymuje dostęp do widoku, w którym fiszki są przedstawione przejrzyście w siatce.
    - Każda fiszka w bazie danych posiada opcje edycji i usunięcia.
    - Zmiany dokonane przez użytkownika (edycja lub usunięcie) są natychmiast widoczne w interfejsie.

### US-003
- **ID:** US-003
- **Tytuł:** System powtórek
- **Opis:** Użytkownik przechodzi do trybu powtórek, gdzie losowo wybrane fiszki (od 5 do 15) są prezentowane w kolejności losowej.
- **Kryteria akceptacji:**
    - Użytkownik ma możliwość nawigacji między fiszkami przy użyciu przycisków „poprzedni” i „następny”.
    - Na ostatniej fiszce przycisk „następny” zmienia się na „zakończ”, co przekierowuje do ekranu generowania.
    - System losowo wybiera pomiędzy 5 a 15 fiszkami przy każdym uruchomieniu trybu powtórek.

### US-004
- **ID:** US-004
- **Tytuł:** Decyzja o zapisaniu fiszki
- **Opis:** Użytkownik decyduje, czy dana propozycja fiszki wygenerowana przez AI zostanie zapisana w bazie danych. Decyzja ta odbywa się poprzez interakcję: kliknięcie „akceptuj” zapisuje fiszkę, natomiast kliknięcie „odrzuć” ją pomija. Edycja fiszki przed zatwierdzeniem pozwala na naniesienie poprawek.
- **Kryteria akceptacji:**
    - Po wyświetleniu fiszek, użytkownik ma możliwość wyboru pomiędzy przyciskiem „akceptuj” (zapisuje fiszkę w bazie danych) a przyciskiem „odrzuć” (fiszka nie jest zapisywana).
    - Funkcja edycji umożliwia wprowadzenie poprawek przed finalnym zatwierdzeniem.
    - Decyzja użytkownika wpływa jedynie na zapis fiszki w bazie, nie modyfikując algorytmu powtórek.

### US-005
- **ID:** US-005
- **Tytuł:** Losowa selekcja fiszek do powtórek
- **Opis:** System automatycznie wybiera losowo fiszki do sesji powtórek, dobierając od 5 do 15 elementów.
- **Kryteria akceptacji:**
    - Podczas inicjalizacji sesji powtórek system losowo wybiera liczbę fiszek w przedziale od 5 do 15.
    - Użytkownik otrzymuje wybrany zestaw w interfejsie powtórek z możliwością nawigacji między nimi.

### US-006
- **ID:** US-006
- **Tytuł:** Rejestracja użytkownika
- **Opis:** Aby zapewnić bezpieczny dostęp oraz personalizację zapisanych fiszek, użytkownik musi przejść przez proces rejestracji w systemie.
- **Kryteria akceptacji:**
    - Użytkownik ma dostęp do formularza rejestracji.
    - Formularz rejestracji wymaga podania niezbędnych danych, takich jak adres email oraz hasło (oraz potwierdzenie hasła).
    - System weryfikuje poprawność wprowadzonych danych i umożliwia zakończenie procesu rejestracji.
    - Po pomyślnej rejestracji użytkownik otrzymuje potwierdzenie na adres email lub w interfejsie.

### US-007
- **ID:** US-007
- **Tytuł:** Logowanie użytkownika
- **Opis:** Użytkownik loguje się do systemu, aby uzyskać dostęp do swoich zapisanych fiszek oraz personalizowanego doświadczenia.
- **Kryteria akceptacji:**
    - Użytkownik ma dostęp do formularza logowania.
    - Formularz logowania wymaga wprowadzenia poprawnych danych uwierzytelniających (adres email oraz hasło).
    - System weryfikuje dane logowania i umożliwia dostęp do konta użytkownika po pomyślnej autoryzacji.
    - W przypadku błędnych danych użytkownik otrzymuje odpowiedni komunikat oraz możliwość ponownej próby logowania.

## 6. Metryki sukcesu
1. **Akceptacja wygenerowanych fiszek:**
    - Minimum 75% proponowanych fiszek generowanych przez AI powinno zostać zaakceptowanych przez użytkownika (poprzez kliknięcia „akceptuj” lub po edycji).

2. **Wykorzystanie AI:**
    - Przynajmniej 75% wygenerowanych fiszek stworzonych przy użyciu funkcji AI powinno być zatwierdzonych i wykorzystanych w procesie nauki.

3. **Intuicyjność interfejsu:**
    - Użytkownicy powinni bez problemu odnaleźć funkcje dodawania tekstu, przeglądania, edycji, usuwania fiszek i trybu powtórek.
    - Opinie użytkowników (poprzez bezpośrednią obserwację i feedback) wskażą, czy podział na sekcje (generowanie, zarządzanie, powtórki) jest przejrzysty i intuicyjny.

4. **Stabilność i szybkość działania:**
    - System powinien poprawnie wyświetlać fiszki oraz umożliwić płynną nawigację między trybami.
    - Czas generowania fiszek oraz przetwarzania powtórek nie powinien przekraczać oczekiwanych granic czasowych dla przeglądarek desktopowych.

5. **Bezpieczeństwo:**
    - System uwierzytelniania powinien zapewnić bezpieczny dostęp do kont użytkowników oraz chronić ich dane (fiszki).

Weryfikacja powyższych metryk będzie przeprowadzana na podstawie danych użytkowników i bezpośrednich testów funkcjonalnych aplikacji.