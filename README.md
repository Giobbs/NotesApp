# NoteAllix

NoteAllix è un'app Android per la gestione avanzata di note, progettata per essere veloce, modulare e scalabile. L’app permette la creazione, modifica, organizzazione e sincronizzazione logica delle note con funzionalità di filtro, pin, ordinamento e import/export.

---

## 🚀 Funzionalità principali

* Creazione, modifica ed eliminazione note
* Sistema di **pin** per evidenziare note importanti
* Ordinamento dinamico (data, titolo, priorità)
* Filtri personalizzati e aggregazioni
* Ricerca rapida tramite SearchView
* Import / Export note
* Persistenza locale con database
* Interfaccia Material Design
* Supporto a tema chiaro/scuro

---

## 🧱 Architettura

Il progetto segue un’architettura MVVM:

* **UI Layer**: Activity + RecyclerView + Adapter
* **ViewModel Layer**: gestione stato UI e logica
* **Repository Layer**: astrazione accesso dati
* **Data Layer**: Room Database (DAO + Entity)

---

## 🛠️ Tecnologie utilizzate

* Java
* Android SDK
* AndroidX
* Room (SQLite ORM)
* LiveData & ViewModel
* Material Components
* RecyclerView

---

## 📂 Struttura progetto

```
com.example.notesapp
│
├── data
│   ├── local
│   │   ├── Note.java
│   │   ├── NoteDao.java
│   │   ├── AppDatabase.java
│   │   └── SortType.java
│   │
│   └── repository
│       └── NoteRepository.java
│
├── ui
│   ├── main
│   │   ├── MainActivity.java
│   │   ├── NotesViewModel.java
│   │   ├── NoteAdapter.java
│   │
│   └── settings
│       └── SettingsActivity.java
│
└── utils
```

---

## ⚙️ Installazione

1. Clona il repository

```bash
git clone https://github.com/Giobbs/NotesApp.git
```

2. Apri il progetto con **Android Studio**

3. Sincronizza Gradle

4. Avvia su emulatore o dispositivo fisico

---

## 🧠 Logica di sistema

Le note vengono gestite tramite:

* `NoteViewModel` → stato UI e comunicazione con repository
* `NoteRepository` → interfaccia unica verso Room
* `NoteDao` → query SQL

Il sistema supporta aggiornamenti reattivi tramite LiveData.

---

## 📌 Feature avanzate

### Pin delle note

Le note possono essere fissate in alto tramite flag `isPinned`.

### Sorting dinamico

Le note possono essere ordinate per:

* Data
* Titolo
* Stato pin

### Filtri

Sistema di filtro per:

* testo
* data
* stato

---

## 🎨 UI/UX

* Material CardView per ogni nota
* Animazioni leggere su interazioni
* Layout responsive
* Supporto dark mode

---

## 🔧 Configurazioni future

* Sync cloud
* Login utente
* Tag e categorie avanzate
* Notifiche promemoria

---

## 📄 Licenza

Progetto privato / sviluppo personale.

---

## 👤 Autore

Sviluppato da Luca
