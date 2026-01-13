# RentMyCar Android App

Een moderne Android-applicatie voor het huren van auto's, gebouwd met Jetpack Compose en Material Design 3.

## Beschrijving

RentMyCar is een gebruiksvriendelijke Android-app waarmee gebruikers gemakkelijk auto's kunnen huren. De applicatie biedt een compleet reserveringssysteem met real-time locatieweergave, rijstatistieken en een intuïtieve gebruikersinterface.

### Belangrijkste functionaliteiten

- **Authenticatie**: Inloggen, registreren en wachtwoord herstellen
- **Auto's zoeken en filteren**: Overzicht van beschikbare auto's met geavanceerde filteropties
- **Auto details**: Gedetailleerde informatie en foto's van beschikbare auto's
- **Reserveringssysteem**: Selecteer data, aantal kilometers en bekijk prijsoverzicht
- **Betalingsoverzicht**: Controleer je boeking en betaalmethode
- **Kaartweergave**: Bekijk de locatie van auto's op een interactieve kaart
- **Profielbeheer**: Beheer je persoonlijke gegevens
- **Rijstatistieken**: Volg je ritten en bekijk statistieken
- **Reserveringsoverzicht**: Bekijk al je actieve en historische reserveringen

## Vereisten

### Systeemvereisten

- **Android Studio**: Hedgehog (2023.1.1) of nieuwer
- **JDK**: Java 11 of hoger
- **Android SDK**:
  - Minimum SDK: 24 (Android 7.0)
  - Target SDK: 36
  - Compile SDK: 36
- **Gradle**: 8.x (gebruikt via Gradle Wrapper)

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

### 5. API configuratie (optioneel)

Als de app verbinding maakt met een backend API, configureer dan de API base URL in het project.

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
   - Gebruik het filtericoon om te filteren op prijs, type, etc.

2. **Auto details**:
   - Tik op een auto om meer details te zien
   - Bekijk foto's, specificaties en locatie op de kaart

3. **Reservering maken**:
   - Klik op "Reserveren"
   - Selecteer start- en einddatum
   - Voer het geschatte aantal kilometers in
   - Controleer het prijsoverzicht
   - Kies een betaalmethode

4. **Reserveringen beheren**:
   - Ga naar het reserveringsoverzicht via het menu
   - Bekijk je actieve en eerdere reserveringen
   - Tik op de locatie-knop om de auto op de kaart te zien

### Extra functies

- **Profiel**: Beheer je persoonlijke gegevens via het profielscherm
- **Rijstatistieken**: Bekijk je rijgeschiedenis en statistieken
- **Driving Tracker**: Volg je huidige rit
- **Wachtwoord vergeten**: Gebruik de "Wachtwoord vergeten" optie op het inlogscherm

## Technologieën

- **Kotlin**: Programmeertaal
- **Jetpack Compose**: Moderne UI toolkit
- **Material Design 3**: Design system
- **Hilt**: Dependency injection
- **Retrofit**: Netwerkcommunicatie
- **Coil**: Image loading
- **MapLibre**: Kaartweergave
- **Navigation Compose**: Navigatie
- **DataStore**: Lokale data opslag

## Projectstructuur

```
app/src/main/
├── java/com/example/rentmycar_android_app/
│   ├── ui/                    # UI componenten en screens
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── CarDetailScreen.kt
│   │   ├── ReservationScreen.kt
│   │   └── ...
│   ├── navigation/            # Navigatie logica
│   ├── data/                  # Data laag (repositories, API)
│   ├── domain/                # Business logica
│   └── MainActivity.kt
└── res/                       # Resources (layouts, strings, etc.)
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