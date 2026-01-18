# RentMyCar Android App

Een moderne Android-applicatie voor het huren van auto's, gebouwd met Jetpack Compose en Material Design 3.

## Beschrijving

RentMyCar is een gebruiksvriendelijke Android-app waarmee gebruikers gemakkelijk auto's kunnen huren. De applicatie biedt een compleet reserveringssysteem met real-time locatieweergave, rijstatistieken en een intuïtieve gebruikersinterface.

### Belangrijkste functionaliteiten

- **Authenticatie**: Inloggen, registreren en wachtwoord herstellen met JWT tokens
- **Auto's zoeken en filteren**: Overzicht van beschikbare auto's met geavanceerde filteropties
- **Auto details**: Gedetailleerde informatie en foto's van beschikbare auto's
- **Reserveringssysteem**: Selecteer data, aantal kilometers en bekijk dynamisch prijsoverzicht
- **Betalingssysteem**: Kies betaalmethode, controleer boeking en verwerk betalingen
- **Kaartweergave**: Bekijk de locatie van auto's op een interactieve MapLibre kaart
- **Auto verhuren**: Voeg je eigen auto's toe met foto's, beheer en verwijder listings
- **Profielbeheer**: Beheer je persoonlijke gegevens en bekijk bonuspunten
- **Rijstatistieken**: Volg je ritten en bekijk uitgebreide statistieken
- **Driving Tracker**: Real-time GPS tracking tijdens het rijden
- **Reserveringsoverzicht**: Bekijk, beheer en annuleer je reserveringen

## Vereisten

### Systeemvereisten

- **Android Studio**: Hedgehog (2023.1.1) of nieuwer
- **JDK**: Java 17 of hoger
- **Android SDK**:
  - Minimum SDK: 24 (Android 7.0)
  - Target SDK: 34
  - Compile SDK: 36
- **Gradle**: 8.x (gebruikt via Gradle Wrapper)
- **Backend API**: Draaiende RentMyCar backend server

### Apparaatvereisten

- Android 7.0 (API 24) of hoger
- Internetverbinding (vereist)
- Locatieservices (optioneel, voor kaartfunctionaliteit)

## Installatie

### 1. Repository klonen

```bash
git clone <repository-url>
cd RentMyCar-Android-App
```

### 2. Project openen in Android Studio

1. Open Android Studio
2. Selecteer "Open" en navigeer naar de gekloonde projectmap
3. Wacht tot Gradle synchronisatie is voltooid

### 3. SDK configureren

Zorg ervoor dat Android SDK 36 is geïnstalleerd:

1. Ga naar **Tools > SDK Manager**
2. Selecteer het tabblad **SDK Platforms**
3. Vink **Android API 36** aan
4. Klik op **Apply** en wacht tot de installatie is voltooid

### 4. Afhankelijkheden installeren

De afhankelijkheden worden automatisch geïnstalleerd tijdens de Gradle sync. Als dit niet automatisch gebeurt:

```bash
./gradlew build
```

### 5. Backend API configuratie

De app vereist een draaiende backend server. De standaard configuratie is:

- **Emulator**: `http://10.0.2.2:8080/` (automatisch geconfigureerd)
- **Fysiek apparaat**: Pas de base URL aan in `NetworkModule.kt`

**Let op**: De app gebruikt `10.0.2.2` als localhost adapter voor de Android emulator. Voor fysieke apparaten moet je de URL wijzigen naar het IP-adres van je backend server.

## De applicatie uitvoeren

### Op een fysiek apparaat

1. Schakel **Developer Options** in op je Android-apparaat:
   - Ga naar **Instellingen > Over telefoon**
   - Tik 7 keer op **Build number**
2. Schakel **USB Debugging** in:
   - Ga naar **Instellingen > Developer Options**
   - Zet **USB Debugging** aan
3. Sluit je apparaat aan via USB
4. Klik in Android Studio op de **Run** knop (groene pijl) of druk op `Shift + F10`
5. Selecteer je apparaat uit de lijst

### Op een emulator

1. Open **AVD Manager** in Android Studio (**Tools > Device Manager**)
2. Klik op **Create Virtual Device**
3. Kies een apparaat (bijv. Pixel 5)
4. Selecteer een system image (Android 7.0 of hoger)
5. Klik op **Finish**
6. Start de emulator
7. Klik op de **Run** knop in Android Studio

### Via opdrachtregel

```bash
# Debug build maken en installeren
./gradlew installDebug

# Release build maken
./gradlew assembleRelease
```

## Gebruik

### Eerste keer opstarten

1. **Registreren**:
   - Open de app
   - Klik op "Registreren"
   - Vul je gegevens in en maak een account aan

2. **Inloggen**:
   - Voer je email en wachtwoord in
   - Klik op "Inloggen"

### Auto huren

1. **Auto's bekijken**:
   - Bekijk de lijst met beschikbare auto's op het startscherm
   - Gebruik het filtericoon om te filteren op prijs, type, locatie, etc.
   - Bekijk auto's op een interactieve kaart

2. **Auto details**:
   - Tik op een auto om meer details te zien
   - Bekijk foto's, specificaties en locatie op de kaart
   - Zie de exacte locatie via MapLibre kaartweergave

3. **Reservering maken**:
   - Klik op "Reserveren"
   - Selecteer start- en einddatum
   - Voer het geschatte aantal kilometers in
   - Controleer het dynamische prijsoverzicht
   - Kies een betaalmethode
   - Bevestig je betaling

4. **Reserveringen beheren**:
   - Ga naar het reserveringsoverzicht via het menu
   - Bekijk je actieve en eerdere reserveringen
   - Annuleer reserveringen indien nodig
   - Tik op de locatie-knop om de auto op de kaart te zien

### Auto verhuren (eigenaar)

1. **Auto toevoegen**:
   - Ga naar "Mijn Auto's" via het menu
   - Klik op "Auto toevoegen"
   - Vul de autogegevens in (merk, model, prijs, etc.)
   - Upload foto's van je auto
   - Stel de locatie in

2. **Auto beheren**:
   - Bekijk al je verhuurde auto's
   - Bewerk autogegevens en prijzen
   - Verwijder auto's uit het aanbod
   - Bekijk reserveringen per auto

### Extra functies

- **Profiel**: Beheer je persoonlijke gegevens via het profielscherm
- **Bonuspunten**: Bekijk je gespaard bonuspunten
- **Rijstatistieken**: Bekijk je rijgeschiedenis en statistieken
- **Driving Tracker**: Volg je huidige rit met real-time GPS tracking
- **Wachtwoord vergeten**: Gebruik de "Wachtwoord vergeten" optie op het inlogscherm

## Technologieën

- **Kotlin**: Programmeertaal
- **Jetpack Compose**: Moderne UI toolkit
- **Material Design 3**: Design system
- **Hilt**: Dependency injection
- **Retrofit + OkHttp**: Netwerkcommunicatie met JWT authenticatie
- **Coil**: Image loading
- **MapLibre**: Interactieve kaartweergave
- **Google Play Services Location**: GPS en locatieservices
- **Nominatim API**: Geocoding (OpenStreetMap)
- **OSRM API**: Route berekeningen
- **Navigation Compose**: Navigatie
- **DataStore**: Lokale data opslag
- **Coroutines + StateFlow**: Async en reactive state management

## Projectstructuur

```
app/src/main/java/com/example/rentmycar_android_app/
├── ui/                    # UI componenten en screens (26+ screens)
│   ├── HomeScreen.kt
│   ├── LoginScreen.kt
│   ├── CarDetailScreen.kt
│   ├── ReservationScreen.kt
│   ├── MyCarsScreen.kt
│   ├── AddCarScreen.kt
│   ├── PaymentScreen.kt
│   ├── DrivingTrackerScreen.kt
│   ├── DrivingStatsScreen.kt
│   └── ...
├── viewmodels/            # ViewModels voor state management
├── navigation/            # Navigatie logica (NavGraph)
├── network/               # API services en DTOs
│   ├── AuthService.kt
│   ├── CarService.kt
│   ├── ReservationService.kt
│   ├── PaymentService.kt
│   └── ...
├── data/                  # Repository implementaties
├── domain/                # Interfaces en validators
├── di/                    # Hilt dependency injection modules
├── model/                 # Data classes
└── util/                  # Helper functies

app/src/main/res/          # Resources (layouts, strings, etc.)
```

## Probleemoplossing

### Gradle sync mislukt

```bash
# Clean het project
./gradlew clean

# Invalidate caches in Android Studio
File > Invalidate Caches > Invalidate and Restart
```

### App crasht bij opstarten

- Controleer of je internetverbinding hebt
- Controleer de logcat in Android Studio voor foutmeldingen
- Zorg ervoor dat alle permissies zijn toegestaan

### Kaart wordt niet weergegeven

- Controleer of locatiepermissies zijn toegestaan
- Zorg ervoor dat locatieservices zijn ingeschakeld op je apparaat

## Licentie

Schoolproject - RentMyCar Android App

## Contact

Voor vragen of problemen, neem contact op met de ontwikkelaars.