# SaveAny — Android client

A native Android app that puts every feature of [SaveAny-Bot](../README.md) in your pocket. It is a thin, beautiful client over the bot's HTTP API, so the same server can be controlled from a phone, a desktop browser and Telegram interchangeably.

> **English** | [العربية](#النسخة-العربية)

## Highlights

- **All seven task types** the bot supports — direct links, yt-dlp, Aria2, Telegram files, Telegraph pictures, parsed plugins, cross-storage transfer.
- **Live progress**: animated gradient bars, percentage and instantaneous speed, polled in the background.
- **Quick Save** + **Share Target**: paste a URL or share from any app (YouTube, Twitter, Browser, magnets) and the right task type is auto-selected.
- **Multi-server**: configure as many SaveAny-Bot deployments as you want, switch between them with a tap.
- **Material You** dynamic colour, light/dark/system themes, full RTL Arabic localisation.
- **Compose-only** — no XML layouts; tiny APK (~12 MB release).

## Architecture

```
android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml        ← MainActivity + ShareReceiverActivity
│   │   ├── kotlin/com/krau/saveany/
│   │   │   ├── SaveAnyApp.kt          ← Application + manual DI container
│   │   │   ├── MainActivity.kt
│   │   │   ├── ShareReceiverActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── api/               ← Retrofit + DTOs (mirrors api/types.go)
│   │   │   │   ├── prefs/             ← DataStore-backed SettingsRepository
│   │   │   │   └── repo/              ← SaveAnyRepository facade
│   │   │   └── ui/
│   │   │       ├── theme/             ← Material 3 / Material You
│   │   │       ├── components/        ← GradientProgress
│   │   │       ├── screens/home, tasks, create, storages, settings, share
│   │   │       └── nav/Routes.kt
│   │   └── res/
│   │       ├── values/                ← English strings, themes, colours
│   │       └── values-ar/             ← Arabic translation
└── build.gradle.kts (Kotlin 2.0 + Compose plugin)
```

The app talks to the bot through `api/v1/*` endpoints, exactly the same routes used by `api/server.go` in the Go server.

## Build

You need:

- JDK 17
- Android SDK with `platforms;android-34` and `build-tools;34.0.0`

```bash
cd android
./gradlew :app:assembleDebug      # debug APK
./gradlew :app:assembleRelease    # release APK (signed with the debug keystore by default)
```

Outputs land in `android/app/build/outputs/apk/`.

To produce an installable release for distribution, replace the `signingConfig` in `app/build.gradle.kts` with your own keystore.

## Configuring a server

1. Open the app → **Settings** → tap the **+** button.
2. Provide a display name, the base URL of your `saveany-bot` HTTP API (for example `http://192.168.1.10:8080`) and the bearer token you set as `[api].token` in `config.toml`.
3. Tap **Test connection** — the app calls `/health` on the server and shows the status.
4. Hit **Save**. The first server becomes the active one automatically.

The HTTP API of the bot must be enabled in `config.toml`:

```toml
[api]
enable = true
host = "0.0.0.0"
port = 8080
token = "your-secret-token"
```

## Quick Save / Share Target

The launcher has a single text field on the Home screen. Paste any URL or magnet link and tap the send icon — the app picks the right task type for you (yt-dlp for YouTube/Twitter/TikTok/etc., Aria2 for magnets, Telegram-files for `t.me/...`, Telegraph for `telegra.ph/...`, otherwise direct download).

The app is also registered as a system **Share Target** for `text/plain`, `http(s)://` and `magnet:` URIs, so sharing from any other app opens the create-task screen with the link pre-filled.

## النسخة العربية

تطبيق Android أصلي يضع كل ميزات بوت SaveAny بين يديك:

- جميع أنواع المهام السبعة: روابط مباشرة، yt-dlp، Aria2، ملفات Telegram، صور Telegraph، إضافات Parsed، النقل بين التخزينات.
- شريط تقدّم متحرّك بتدرّج لوني وسرعة لحظية.
- "حفظ سريع" + الاستقبال من زر "مشاركة" في أي تطبيق آخر، مع تخمين تلقائي لنوع المهمة.
- إدارة عدة خوادم SaveAny-Bot دفعة واحدة، والتبديل بينها بنقرة.
- ثيم Material You ديناميكي، فاتح/داكن، ودعم كامل للعربية واتجاه RTL.

اضبط الخادم من شاشة "الإعدادات"، ضع رابط الـ API ورمز Bearer ثم اضغط "اختبار الاتصال". الباقي يكتمل تلقائياً.
