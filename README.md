# 🧘 MicroHabits — Application Android (Jetpack Compose)

Application Android de micro-habitudes gamifiée, construite avec **Kotlin + Jetpack Compose** et une architecture **MVVM + Room**.

---

## 🏗️ Architecture

```
MicroHabits/
├── app/src/main/java/com/microhabits/
│   ├── data/
│   │   ├── model/          → Entités Room (Habit, Completion, DailyStats)
│   │   ├── db/             → AppDatabase + HabitDao
│   │   └── repository/     → HabitRepository (source de vérité unique)
│   ├── di/                 → Module Hilt (injection DB + DAO)
│   ├── viewmodel/
│   │   ├── HabitViewModel  → État Home + Stats + actions utilisateur
│   │   └── FocusViewModel  → Timer du mode Focus
│   ├── ui/
│   │   ├── theme/          → Couleurs, typographie, thème Material3
│   │   ├── components/     → Composants réutilisables (CircularProgress, HabitCard…)
│   │   ├── screens/
│   │   │   ├── HomeScreen  → Liste drag & drop, progression circulaire, XP
│   │   │   ├── FocusScreen → Timer plein écran, mode épuré
│   │   │   └── StatsScreen → Graphiques Vico, tableau de bord
│   │   └── AppNavGraph.kt  → Navigation Compose
│   ├── MainActivity.kt
│   └── MicroHabitsApp.kt   → @HiltAndroidApp
├── build.gradle.kts
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

---

## 🚀 Fonctionnalités

### 🏠 Écran principal
- **Barre de progression circulaire** qui change de couleur selon l'avancement (rouge → violet → vert)
- **Drag & Drop** pour réordonner les habitudes (bibliothèque `reorderable`)
- **Validation par tap** sur le cercle check → animation bounce + snackbar de récompense
- **Système XP + Niveaux** : chaque habitude donne des XP, le niveau monte progressivement
- Habitudes pré-chargées au premier lancement (5 micro-habitudes par défaut)
- **Ajout d'habitude** : nom, emoji, durée, XP et couleur personnalisables

### ⏱️ Mode Focus
- Interface **entièrement épurée** — une seule tâche visible
- **Timer décompte** avec animation de pulsation quand il tourne
- Boutons Start / Pause / Reset
- Valide automatiquement l'habitude quand le timer arrive à 0
- Bouton retour pour quitter sans valider

### 📊 Statistiques
- **Graphique en barres** (30 derniers jours) via Vico
- Cartes récapitulatives : XP total, jours actifs, nb de complétions, niveau actuel
- Historique des 7 derniers jours

---

## 🛠️ Stack technique

| Couche | Technologie |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + StateFlow) |
| Base de données | Room (SQLite) |
| Injection | Hilt |
| Drag & Drop | `sh.calvin.reorderable` |
| Graphiques | Vico (`com.patrykandpatrick.vico`) |
| Async | Kotlin Coroutines + Flow |

---

## ⚙️ Prérequis & Installation

### Prérequis
- **Android Studio Ladybug** (2024.2.1) ou plus récent
- **JDK 17**
- **SDK Android 26+** (minSdk) / **SDK 35** (compileSdk)

### Étapes

1. **Cloner / décompresser** le projet
```bash
# ou décompresser MicroHabits.zip dans un dossier
```

2. **Ouvrir dans Android Studio**
   - `File → Open` → sélectionner le dossier `MicroHabits/`
   - Attendre la synchronisation Gradle

3. **Lancer**
   - Connecter un appareil Android (API 26+) ou lancer un émulateur
   - `Run → Run 'app'` (ou `Shift+F10`)

> **Note** : La première synchronisation Gradle télécharge les dépendances (~200 Mo). Assure-toi d'avoir une bonne connexion.

---

## 🎨 Personnalisation rapide

### Ajouter une couleur d'habitude
Dans `HomeScreen.kt`, la liste `colors` du dialog :
```kotlin
val colors = listOf("#6C63FF","#2ECF8A","#FF6584","#FFB547","#9B59B6","#4FC3F7")
```

### Modifier la formule des niveaux XP
Dans `HabitViewModel.kt`, fonction `computeLevel()` :
```kotlin
// Actuellement : niveau N coûte N × 100 XP
while (xpLeft >= level * 100) { ... }
// Exemple courbe plus douce : level * 80
```

### Changer le thème de couleur
Dans `ui/theme/Theme.kt`, modifier les constantes :
```kotlin
val Indigo500 = Color(0xFF6C63FF)  // Couleur principale
val Green400  = Color(0xFF2ECF8A)  // Succès / complétion
val Pink400   = Color(0xFFFF6584)  // Accent secondaire
```

---

## 📁 Prochaines étapes suggérées

- [ ] **Notifications** : rappels quotidiens avec WorkManager
- [ ] **Widgets Android** : Glance API pour une habitude sur l'écran d'accueil
- [ ] **Haptic feedback** : vibration à la validation
- [ ] **Thème clair** : basculer light/dark selon les préférences système
- [ ] **Export CSV** : historique des stats
- [ ] **Trophées** : déverrouillage de badges (7 jours de suite, 100 XP, etc.)
- [ ] **Cloud sync** : Firebase Firestore pour synchroniser entre appareils

---

## 🐛 Problèmes fréquents

| Erreur | Solution |
|---|---|
| `Hilt component not found` | Vérifier `@HiltAndroidApp` sur `MicroHabitsApp` |
| Crash Room au lancement | Incrémenter `version` dans `@Database` ou `fallbackToDestructiveMigration()` |
| Graphique vide | Il faut au moins 1 complétion enregistrée en base |
| Drag & Drop ne fonctionne pas | S'assurer que `key = { it.habit.id }` est bien passé dans `items()` |
