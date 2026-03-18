# Metro Stamp App

Monorepo cho mobile app system cua Metro Stamp, tach rieng khoi web/admin.

## Tech Stack

- Mobile: Flutter
- Backend: Java Spring Boot
- Primary database: PostgreSQL
- Secondary store: MongoDB

## Repository Structure

- `mobile/`: Flutter app cho nguoi dung cuoi
- `backend/`: Spring Boot API cho mobile app
- `docs/`: architecture, API, database va business rules
- `infra/`: Dockerfile, env va script local setup

## Data Strategy

- PostgreSQL la source of truth cho du lieu nghiep vu chinh
- MongoDB dung cho logs, events, analytics va payload linh hoat

## Current Status

- Da dung xong khung source cho Flutter app
- Da dung xong khung source cho Spring Boot backend
- Da co docker-compose cho PostgreSQL, MongoDB va API
- Da co migration PostgreSQL khoi tao ban dau
- Chua co business logic day du va chua bootstrap Flutter project bang `flutter create`

## How To Use Source

### Mobile

- Entry point: `mobile/lib/main.dart`
- App shell: `mobile/lib/app/app.dart`
- Routing: `mobile/lib/app/router.dart`
- Shared services: `mobile/lib/core/services/`
- Business features: `mobile/lib/features/`

Feature uu tien hien tai:

- `auth`
- `station`
- `scan`
- `stamp_book`
- `rewards`

### Backend

- Entry point: `backend/src/main/java/com/metrostamp/api/ApiApplication.java`
- Config: `backend/src/main/resources/application*.yml`
- PostgreSQL migration: `backend/src/main/resources/db/migration/`
- Business modules: `backend/src/main/java/com/metrostamp/api/modules/`

Module uu tien hien tai:

- `auth`
- `users`
- `stations`
- `scans`
- `stamps`
- `rewards`

## Run Local

### 1. Prerequisites

Can co san:

- Flutter SDK
- Java 21
- Docker Desktop
- Gradle hoac Gradle Wrapper

Luu y:

- Thu muc `mobile/` hien moi la scaffold source, chua phai Flutter project day du do chua co `android/`, `ios/`, `test/`
- Thu muc `backend/` da co source Spring Boot nhung chua co Gradle Wrapper

### 2. Start databases va API bang Docker

Chay tu root repo:

```bash
docker compose up -d postgres mongo api
```

Hoac dung script:

```bash
./infra/scripts/start-local.sh
```

Dung stack:

```bash
./infra/scripts/stop-local.sh
```

API mac dinh:

- `http://localhost:8080`
- Health endpoint: `http://localhost:8080/actuator/health`
- Auth placeholder: `http://localhost:8080/api/v1/auth/health`

### 3. Run backend khong dung Docker

Neu may da co Gradle:

```bash
cd backend
gradle bootRun --args='--spring.profiles.active=local'
```

Hoac neu sau nay them Gradle Wrapper:

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Run mobile

Hien tai `mobile/` la scaffold source. De chay Flutter app, buoc tiep theo nen bootstrap project:

```bash
cd mobile
flutter create .
flutter pub get
flutter run
```

Sau khi bootstrap, can merge lai neu Flutter tao them file mac dinh trung voi source da co.

## Suggested Next Steps

1. Bootstrap `mobile/` thanh Flutter project day du.
2. Them Gradle Wrapper cho `backend/`.
3. Hoan thien `auth`, `stations`, `rewards`, `scans`.
4. Noi mobile voi API that.
