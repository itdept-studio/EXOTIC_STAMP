# Flutter Project Structure & Build Pipeline

> Tài liệu chuẩn về cấu trúc thư mục và working pipeline cho Flutter project theo kiến trúc Feature-first DDD.

---

## Working Pipeline

### Development flow

```
Source Code
    │
    ▼
flutter pub get
    │   Resolve & download dependencies (pubspec.yaml)
    ▼
dart run build_runner build --delete-conflicting-outputs
    │   Code generation: freezed, json_serializable, flutter_gen, GoRouter
    ▼
flutter analyze --fatal-infos
    │   Static analysis: linting, type checking
    ▼
flutter test --coverage
    │   Unit / widget / integration tests
    ▼
flutter build [target]
    │
    ├── flutter build apk --release --obfuscate --split-debug-info
    ├── flutter build ipa --release
    ├── flutter build web --release
    └── flutter build linux / windows / macos
```

### CI/CD pipeline (GitHub Actions / Codemagic)

```
git push → trigger CI
    │
    ├── Setup
    │   ├── actions/setup-java@v4
    │   ├── subosito/flutter-action@v2  (hoặc FVM)
    │   └── flutter pub get
    │
    ├── Code Generation
    │   └── dart run build_runner build --delete-conflicting-outputs
    │
    ├── Quality Gate
    │   ├── flutter analyze --fatal-infos
    │   └── flutter test --coverage
    │
    ├── Build
    │   ├── flutter build apk --release --obfuscate --split-debug-info=./symbols
    │   └── flutter build ipa --release (macOS runner)
    │
    └── Deploy
        ├── Upload artifact to GitHub Releases
        ├── Deploy to Firebase App Distribution (staging)
        └── Deploy to Play Store / TestFlight (production)
```

### Các lệnh hay dùng

| Lệnh | Mục đích |
|------|----------|
| `flutter pub get` | Tải dependencies |
| `flutter pub upgrade` | Nâng version deps |
| `dart run build_runner build` | Chạy code generation một lần |
| `dart run build_runner watch` | Chạy code generation liên tục |
| `flutter analyze` | Static analysis |
| `flutter test` | Chạy toàn bộ test |
| `flutter test --coverage` | Test + coverage report |
| `flutter clean` | Xoá build cache |
| `fvm use stable` | Đổi Flutter version (FVM) |
| `flutter build apk --release` | Build Android release |
| `flutter build ipa --release` | Build iOS release |

---

## ASCII Treemap Structure

```
my_app/
├── lib/                                  ← toàn bộ Dart source code
│   ├── main.dart                         ← entry point, runApp()
│   ├── app.dart                          ← MaterialApp / GoRouter setup
│   │
│   ├── core/                             ← cross-cutting concerns
│   │   ├── di/                           ← dependency injection (GetIt, Riverpod)
│   │   ├── router/                       ← GoRouter config, route names
│   │   ├── constants/                    ← app-wide constants, env config
│   │   ├── error/                        ← failure types, exception handlers
│   │   └── utils/                        ← helpers, extensions
│   │
│   ├── features/                         ← tách theo tính năng (feature-first)
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   │   ├── datasources/          ← remote / local datasource
│   │   │   │   ├── models/               ← DTOs + fromJson/toJson
│   │   │   │   └── repositories/         ← impl của domain repo
│   │   │   ├── domain/
│   │   │   │   ├── entities/             ← pure Dart entities
│   │   │   │   ├── repositories/         ← abstract repo interface
│   │   │   │   └── usecases/             ← business logic (1 class = 1 usecase)
│   │   │   └── presentation/
│   │   │       ├── bloc/                 ← BLoC / Cubit + state + event
│   │   │       ├── screens/              ← LoginScreen, RegisterScreen
│   │   │       └── widgets/              ← AuthButton, OtpField
│   │   │
│   │   ├── home/
│   │   │   └── …                         ← same structure as auth/
│   │   │
│   │   └── …                             ← other features
│   │
│   ├── shared/                           ← dùng chung toàn app
│   │   ├── widgets/                      ← AppButton, AppTextField, EmptyState
│   │   ├── theme/                        ← AppTheme, colors, text styles
│   │   └── extensions/                   ← BuildContext ext, DateTime ext
│   │
│   ├── l10n/                             ← internationalization
│   │   ├── app_en.arb
│   │   └── app_vi.arb
│   │
│   └── gen/                              ← auto-generated (KHÔNG edit tay)
│       ├── assets.gen.dart               ← flutter_gen
│       └── *.g.dart                      ← json_serializable, freezed
│
├── test/                                 ← mirror cấu trúc lib/
│   ├── unit/
│   │   └── features/
│   │       └── auth/
│   │           └── domain/
│   │               └── usecases/
│   │                   └── login_usecase_test.dart
│   ├── widget/
│   └── golden/
│
├── integration_test/                     ← E2E test chạy trên device/emulator
│   └── app_test.dart
│
├── android/                              ← Android native shell
│   ├── app/
│   │   └── build.gradle
│   ├── gradle.properties
│   └── AndroidManifest.xml
│
├── ios/                                  ← iOS native shell
│   ├── Podfile
│   ├── Podfile.lock
│   └── Runner.xcworkspace
│
├── web/                                  ← Flutter Web
│   ├── index.html
│   ├── manifest.json
│   └── favicon.png
│
├── assets/
│   ├── images/
│   ├── fonts/
│   └── mock/                             ← JSON mock data cho dev/test
│
├── pubspec.yaml                          ← deps, assets, flutter config
├── pubspec.lock                          ← commit vào git ✓
├── analysis_options.yaml                 ← linting rules
├── .fvmrc                                ← pin Flutter SDK version (FVM)
└── .gitignore
```

---

## Cấu trúc một Feature (DDD)

Mỗi feature trong `lib/features/` đều có 3 layer:

```
feature_name/
│
├── data/                         ← Infrastructure layer
│   ├── datasources/
│   │   ├── feature_remote_datasource.dart
│   │   └── feature_local_datasource.dart
│   ├── models/
│   │   └── feature_model.dart    ← DTO, có fromJson/toJson
│   └── repositories/
│       └── feature_repository_impl.dart
│
├── domain/                       ← Business logic layer (pure Dart, no Flutter)
│   ├── entities/
│   │   └── feature_entity.dart
│   ├── repositories/
│   │   └── feature_repository.dart   ← abstract interface
│   └── usecases/
│       ├── get_feature_usecase.dart
│       └── update_feature_usecase.dart
│
└── presentation/                 ← UI layer
    ├── bloc/
    │   ├── feature_bloc.dart
    │   ├── feature_event.dart
    │   └── feature_state.dart
    ├── screens/
    │   └── feature_screen.dart
    └── widgets/
        └── feature_card.dart
```

---

## Quy tắc & Best Practices

### Naming convention

| Loại | Convention | Ví dụ |
|------|-----------|-------|
| File | `snake_case.dart` | `user_profile_screen.dart` |
| Class | `PascalCase` | `UserProfileScreen` |
| Variable | `camelCase` | `userProfile` |
| Constant | `camelCase` | `kDefaultTimeout` |
| Private | `_camelCase` | `_isLoading` |

### Quy tắc quan trọng

- **`gen/`** — Không edit tay. Luôn chạy `build_runner` để regenerate.
- **`pubspec.lock`** — Luôn commit vào git để pin version cho cả team.
- **`.fvmrc`** — Commit vào git để mọi người dùng cùng Flutter version.
- **`test/`** — Mirror y chang cấu trúc `lib/`. Ví dụ:
  - `lib/features/auth/domain/usecases/login_usecase.dart`
  - → `test/unit/features/auth/domain/usecases/login_usecase_test.dart`
- **Domain layer** — Không import package Flutter, chỉ pure Dart.
- **`shared/`** — Chỉ chứa widget/util thực sự dùng chung ≥ 2 features.

### .gitignore cần có

```gitignore
# Flutter
.dart_tool/
.flutter-plugins
.flutter-plugins-dependencies
build/

# FVM
.fvm/versions/
.fvm/flutter_sdk

# Generated (tuỳ team, có thể bỏ comment để commit gen files)
# lib/gen/

# IDE
.idea/
.vscode/
*.iml

# iOS
ios/Pods/
ios/.symlinks/

# Android
android/.gradle/
android/local.properties
```

---

## Dependency phổ biến (pubspec.yaml)

```yaml
dependencies:
  flutter:
    sdk: flutter

  # State management
  flutter_bloc: ^8.1.6
  equatable: ^2.0.5

  # DI
  get_it: ^8.0.2
  injectable: ^2.4.4

  # Navigation
  go_router: ^14.2.7

  # Network
  dio: ^5.7.0
  retrofit: ^4.4.1

  # Local storage
  hive_flutter: ^1.1.0
  shared_preferences: ^2.3.2

  # Code quality
  freezed_annotation: ^2.4.4
  json_annotation: ^4.9.0

  # Assets
  flutter_svg: ^2.0.10+1
  cached_network_image: ^3.4.1

dev_dependencies:
  flutter_test:
    sdk: flutter

  # Code generation
  build_runner: ^2.4.12
  freezed: ^2.5.7
  json_serializable: ^6.8.0
  injectable_generator: ^2.6.2
  retrofit_generator: ^9.1.4
  flutter_gen_runner: ^5.7.0

  # Linting
  flutter_lints: ^4.0.0
  # hoặc: very_good_analysis: ^6.0.0
```

---

 
## AI CODE GENERATION COMMON ERRORS

### 1. Architecture & design issues

* No clear layers
* Violate separation of concerns
* Duplicate logic
* Wrong abstraction
* Not following project pattern

---

### 2. Code quality issues

* Magic numbers
* Hardcoding
* Bad naming
* Large functions
* Not reusable

---

### 3. Logic & business issues

* Missing edge cases
* Wrong business logic
* Hidden assumptions
* Missing validation

---

### 4. Dependency issues

* Outdated libraries
* Wrong framework usage
* Unnecessary imports
* Not following best practices

---

### 5. Test & reliability issues

* Only happy path tests
* Incorrect assertions
* Bad mocking
* Missing error tests

---

### 6. Performance issues

* Inefficient loops
* N+1 queries
* Missing caching
* Wrong async handling

---

### 7. Security issues

* No input sanitization
* Secret leaks
* Broken auth logic
* Missing permission checks

---

### 8. Refactor issues

* Breaking behavior
* Missing usage updates
* Silent bugs

---

### 9. Consistency issues

* Inconsistent style
* Naming mismatchßßßßß
* Pattern inconsistency

---

### 10. Workflow issues

* Generating full feature at once
* Applying changes without review
* Losing context
* Over-dependency on AI

---

> **Reminder:** Paste this file at the start of every AI session.
> This version reflects the system state after completing the `user` module. 

*Generated for Flutter DDD project structure — update theo version Flutter và package mới nhất.*
