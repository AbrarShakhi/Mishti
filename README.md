<h1 align="center">Mishti</h1>

<p align="center">
  A language model that runs entirely on your phone.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84">
  <img alt="Min SDK" src="https://img.shields.io/badge/minSdk-30%20(Android%2011)-blue">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-7F52FF">
  <img alt="UI" src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%C2%B7%20Material%203-4285F4">
</p>

---

Mishti is an offline chat app for Android. It downloads a small open-weights model to your
device and runs it locally through [llama.cpp](https://github.com/ggml-org/llama.cpp), so
conversations never leave the phone. After the model is downloaded, no network connection is
needed to use it.

There is no account, no server and no telemetry.

## Download

Signed APKs are published on the releases page:

**→ [github.com/AbrarShakhi/Mishti/releases](https://github.com/AbrarShakhi/Mishti/releases)**

Release builds ship `arm64-v8a` and `x86_64`. Install the APK directly; you will need to allow
installation from unknown sources.

## Screenshots

<!--
Uncomment once the images are in place.

| Onboarding | Chat | Conversations |
|:---:|:---:|:---:|
| <img src="docs/screenshots/onboarding.png" width="240" alt="Onboarding"> | <img src="docs/screenshots/chat.png" width="240" alt="Chat"> | <img src="docs/screenshots/drawer.png" width="240" alt="Navigation drawer"> |

| Models | Settings | Themes |
|:---:|:---:|:---:|
| <img src="docs/screenshots/models.png" width="240" alt="Model library"> | <img src="docs/screenshots/settings.png" width="240" alt="Settings"> | <img src="docs/screenshots/themes.png" width="240" alt="Theme options"> |
-->

## Features

- **Fully offline inference.** llama.cpp runs the model on the device's CPU through a JNI
  bridge. Nothing is sent anywhere.
- **Conversations that persist.** Sessions and messages are stored in a local database, with
  rename, delete and a conversation drawer.
- **Streaming replies.** Tokens appear as they are produced, and generation can be stopped
  mid-reply — the partial answer is kept.
- **A model library.** Download, verify, select and delete models from inside the app.
  Transfers resume after an interruption, continue while the app is in the background, and are
  checked against a SHA-256 before the file is accepted.
- **Device-aware.** Available RAM is checked against each model's requirement *before* any
  bandwidth is spent, rather than after an out-of-memory kill.
- **Tunable.** Temperature, top-p, top-k, response limit, context window, thread count and a
  custom pre-instruction (system prompt) are all exposed in Settings.
- **Throughput readout.** Each reply reports the tokens per second it was generated at.
- **Material 3 throughout.** System / light / dark, dynamic colour from your wallpaper on
  Android 12+ plus four hand-tuned palettes, and a choice of four typefaces.

## Supported models

The catalogue ships with the app, so the model list renders without a connection.

| Model | Parameters | Quantization | Download | RAM required |
|---|---|---|---|---|
| SmolLM2 360M Instruct | 360M | `Q4_K_M` | 258 MB | 3.2 GB |
| Qwen2.5 0.5B Instruct | 0.5B | `Q4_K_M` | 379 MB | 3.2 GB |
| Llama 3.2 1B Instruct | 1B | `Q4_K_M` | 770 MB | 4.0 GB |

Weights are fetched from Hugging Face on first use. Each model carries its own upstream
licence — Llama 3.2 in particular is covered by the Llama 3.2 Community License, not an OSI
licence.

## Requirements

- Android 11 (API 30) or newer
- `arm64-v8a` or `x86_64`
- At least 3.2 GB of RAM reported by the system, and more for the larger models
- Free storage for whichever models you download

## Building from source

The llama.cpp source is a git submodule pinned to a specific tag, so clone recursively:

```bash
git clone --recurse-submodules https://github.com/AbrarShakhi/Mishti.git
cd Mishti
```

If the repository is already cloned:

```bash
git submodule update --init --recursive
```

You will need the Android SDK with **NDK 28.2.13676358** and **CMake 3.22.1** installed, and a
`local.properties` at the project root pointing at the SDK:

```properties
sdk.dir=/path/to/Android/Sdk
```

Then:

```bash
./gradlew assembleDebug          # build the APK
./gradlew installDebug           # build and install on a connected device
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew lintDebug              # Android Lint
```

The first build compiles ggml and llama.cpp from source and takes a while. Debug builds are
restricted to `arm64-v8a` to keep that cost down; release builds add `x86_64`.

## Architecture

Kotlin and Jetpack Compose throughout, with MVI for every stateful screen and Koin for
dependency injection. Code is split by ownership — `common/` for the app shell, navigation,
theme and the engine interface, and `features/<feature>/` for each screen and the layers it
actually needs.

The inference engine sits behind a single `LlmEngine` interface, so the UI, streaming and
persistence are written against a seam rather than against llama.cpp directly.

[`CLAUDE.md`](CLAUDE.md) documents the architecture in full — the MVI contract, the
Navigation 3 back stack, the chrome pattern, the persistence rules, the download subsystem and
the native binding. [`docs/reference-apps.md`](docs/reference-apps.md) records what was learned
from two existing open-source local-LLM apps before any of it was designed.

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.4.20 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3 |
| Navigation | Navigation 3 |
| DI | Koin 4.2 |
| Storage | Room 2.8, DataStore Preferences |
| Networking | Ktor 3.6 |
| Inference | llama.cpp (`b11030`) via JNI, NDK 28.2 |
| Build | AGP 9.4, Gradle 9.7, JVM 17 target |

## Status

Mishti is usable but young. Known limitations, in the interest of not surprising anyone:

- A conversation that outgrows the context window is rejected rather than trimmed.
- Reported tokens per second includes prompt processing, so it under-reports on long chats.
- CPU only — there is no GPU backend.
- Sampling settings are global, not per model.
- Deleting a conversation is confirmed but cannot be undone.

## Credits

- [llama.cpp](https://github.com/ggml-org/llama.cpp) by Georgi Gerganov and contributors — MIT.
- Typefaces: [Inter](https://rsms.me/inter/), [Lora](https://github.com/cyrealtype/Lora-Cyrillic)
  and [JetBrains Mono](https://www.jetbrains.com/lp/mono/), all under the SIL Open Font License.
  Copies of the licences ship in the APK.
- <a href="https://www.flaticon.com/free-icons/sweet" title="sweet icons">Sweet icons created by Magnific — Flaticon</a>
- [Ensu](https://github.com/ente-io/ente/tree/main/android/apps/ensu) (AGPL-3.0) and
  [PocketPal AI](https://github.com/a-ghorbani/pocketpal-ai) (MIT) were studied as prior art.
  Their code was read, not copied.
