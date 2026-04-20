# Memento

Pastelowa aplikacja na Androida do zarządzania przypomnieniami i zadaniami.

## Funkcje

- **Tworzenie mementów** – tytuł, opis, data/godzina, kategoria, priorytet, kolor
- **Powiadomienia i alarmy** – tryb powiadomienia, alarmu lub brak; dźwięk i wibracje opcjonalnie
- **Powtarzanie** – brak, codziennie, co tydzień, co miesiąc
- **Sortowanie** – po dacie, priorytecie, tytule lub dacie dodania
- **Filtrowanie** – zakładki: Wszystkie / Dzisiaj / Nadchodzące / Ukończone; filtr po kategorii
- **Kategorie** – własne kategorie z nazwą i kolorem
- **Pastelowy interfejs** – 8 kolorów kart, 10 kolorów kategorii, tryb jasny i ciemny
- **Ustawienia** – motyw (jasny/ciemny/systemowy), globalne włączanie dźwięku i wibracji

## Architektura

MVVM + Room + Navigation Component + Coroutines/Flow

## Wymagania

- Android 8.0+ (minSdk 26)
- Kotlin 1.9.22, AGP 8.2.2
