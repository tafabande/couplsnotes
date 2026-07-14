# NoteShare

NoteShare is a secure, collaborative note-taking application designed for pairs (like couples or close friends). It allows real-time synchronization, secure vault notes with biometric authentication, mood tracking, and a shared timeline.

## Features

- **Shared Timeline & Notes**: Collaborate on notes in real-time, view a chronological timeline of notes, moods, and events.
- **Biometric Vault Notes**: Secure sensitive notes with AES-GCM encryption. Vault notes require biometric authentication (fingerprint/face unlock) to view.
- **Mood Tracking**: Log your mood and share it instantly with your partner.
- **Offline-First Architecture**: Built with Room Database and Firebase Firestore, the app seamlessly handles offline edits and syncs them automatically when back online.
- **Modern UI**: Built entirely with Jetpack Compose using Material Design 3 guidelines.

## Technology Stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Clean Architecture principles
- **Dependency Injection**: Hilt (Dagger)
- **Local Database**: Room
- **Remote Database**: Firebase Firestore
- **Security**: Android Keystore, `androidx.biometric`

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Configure your Firebase project and add the `google-services.json` file to the `app/` directory.
4. Build and run the app on an emulator or physical device.
