# 🍳 Recipe App

A modern, full-stack Android application built with **Kotlin**, **Jetpack Compose**, and **Supabase**. This project demonstrates clean architecture, MVVM pattern, and modern Android development best practices.

![Status](https://img.shields.io/badge/Status-Active-success)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green)
![Supabase](https://img.shields.io/badge/Supabase-Auth%20%26%20DB-3ECF8E)

## ✨ Features

- **🔐 Authentication**: Secure email/password login and registration using Supabase Auth.
- **🏠 Feed**: Browse latest recipes with pull-to-refresh and infinite scrolling support.
- **🔍 Search**: Real-time recipe search by title.
- **❤️ Favorites**: Save favorite recipes locally for offline access (Room Database).
- **📝 Create & Edit**: Add your own recipes with images, ingredients, and instructions.
- **🌓 Dark Mode**: Fully supported dark/light theme with persistent settings (DataStore).
- **👤 Profile**: Manage your account and view your own recipes.

## 🛠️ Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
- **Architecture**: MVVM + Clean Architecture (Domain/Data/Presentation layers)
- **DI**: [Hilt](https://dagger.dev/hilt/)
- **Backend**: [Supabase](https://supabase.com/) (Auth, Database, Storage)
- **Local DB**: [Room](https://developer.android.com/training/data-storage/room)
- **Network**: [Ktor](https://ktor.io/) (via Supabase SDK)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Navigation**: [Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose) (Type-Safe Routes)
- **Settings**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Concurrency**: Coroutines & Flow

## 🏗️ Architecture

The app follows the principles of Clean Architecture:

```mermaid
graph TD
    UI[Presentation Layer<br/>(Compose, ViewModels)] --> Domain[Domain Layer<br/>(UseCases, Models, Repositories)]
    Data[Data Layer<br/>(Repositories Impl, DTOs)] --> Domain
    Data --> Remote[Remote Data Source<br/>(Supabase)]
    Data --> Local[Local Data Source<br/>(Room, DataStore)]
```

## 🚀 Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/recipe-app.git
   ```

2. **Supabase Configuration**
   - Create a new project on [Supabase Dashboard](https://supabase.com/dashboard).
   - Create a table `recipes` with the following schema:
     ```sql
     create table recipes (
       id uuid default gen_random_uuid() primary key,
       user_id uuid references auth.users(id),
       title text not null,
       ingredients text not null,
       instructions text not null,
       image_url text,
       created_at timestamp with time zone default timezone('utc'::text, now())
     );
     ```
   - Enable RLS and add policies for select, insert, update, delete.
   - Update `SupabaseModule.kt` with your **Project URL** and **Anon Key**.

3. **Build and Run**
   Open the project in Android Studio (Ladybug or later) and run on an emulator/device.

## 📸 Screenshots

| Home | Detail | Add Recipe |
|------|--------|------------|
| ![Home](docs/home.png) | ![Detail](docs/detail.png) | ![Add](docs/add.png) |

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## 📄 License

This project is licensed under the MIT License.
