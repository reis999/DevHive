# DevHive

[![Kotlin](https://img.shields.io/badge/language-Kotlin-blueviolet?logo=kotlin\&logoColor=white)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/database-Firebase-ffca28?logo=firebase\&logoColor=white)](https://firebase.google.com/)
[![License](https://img.shields.io/github/license/reis999/DevHive)](LICENSE)

DevHive is a collaborative study mobile app designed specifically for university students in technology fields. The app provides a platform for students to connect, share resources, and collaborate on their studies.

## Features

* 📚 Study Groups: Join or create groups with peers in your field.
* 🕹️ Real-time Collaboration: Share notes, resources, and ideas instantly.
* 🔒 Secure Authentication: Powered by Firebase.
* 📱 Modern UI: Clean, intuitive, and responsive mobile design.

## Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **Database & Auth**: [Firebase](https://firebase.google.com/)
* **Architecture**: MVVM Clean Architecture

  ```
  Model → Service → Repository → Use Case → ViewModel → Activity
  ```

## Getting Started

### Prerequisites

* [Android Studio](https://developer.android.com/studio)
* [Kotlin](https://kotlinlang.org/) 1.5+
* A [Firebase Project](https://console.firebase.google.com/)

### Setup Instructions

1. **Clone the repository:**

   ```bash
   git clone https://github.com/reis999/DevHive.git
   ```

2. **Create a Firebase project:**

   * Go to [Firebase Console](https://console.firebase.google.com/).
   * Create a new project.
   * Add an Android app to your project and download the `google-services.json` file.

3. **Associate Firebase with the app:**

   * Place your `google-services.json` in the `/app` directory of the project.
   * Sync your project with Gradle files.

4. **Build and run:**

   * Open the project in Android Studio.
   * Build and run on an emulator or physical device.

## Project Structure

```
src\main\java\ipvc\tp\devhive
├───data
│   ├───local
│   │   ├───converter
│   │   ├───dao
│   │   └───entity
│   ├───model
│   ├───remote
│   │   └───service
│   ├───repository
│   └───util
├───di
├───domain
│   ├───model
│   ├───repository
│   └───usecase
│       ├───auth
│       ├───chat
│       ├───comment
│       ├───material
│       ├───studygroup
│       ├───sync
│       └───user
└───presentation
    ├───ui
    │   ├───auth
    │   ├───intro
    │   ├───main
    │   │   ├───chat
    │   │   │   └───adapters
    │   │   ├───comment
    │   │   ├───home
    │   │   ├───material
    │   │   ├───profile
    │   │   ├───settings
    │   │   └───studygroup
    │   │       └───adapters
    │   └───splash
    ├───util
    └───viewmodel
        ├───auth
        ├───chat
        ├───comment
        ├───material
        ├───profile
        └───studygroup
```

## Contributing

Contributions are welcome! To propose a feature or bug fix:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes and push the branch.
4. Open a pull request describing your changes.

## License

This project is licensed under the [MIT License](LICENSE).

---

Designed and maintained by [David Reis](https://github.com/reis999) and [DiogoROliveira](https://github.com/DiogoROliveira)
