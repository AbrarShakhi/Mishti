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

Mishti is a Compose Multiplatform offline chat app targeting Android and iOS. It downloads a small open-weights model to your device
and runs it locally through [llama.cpp](https://github.com/ggml-org/llama.cpp), so conversations
never leave the phone. After the model is downloaded, no network connection is needed to use it.

There is no account, no server and no telemetry.

*Mishti* (মিষ্টি) means *sweet* in Bengali, and its model shop is *Mishtir Bhandar*, a store of
sweets. [More about Mishti →](docs/about.md)

## Download

Signed APKs are published on the releases page:

**→ [github.com/AbrarShakhi/Mishti/releases](https://github.com/AbrarShakhi/Mishti/releases)**

Release builds ship `arm64-v8a` and `x86_64`. Install the APK directly; you will need to allow
installation from unknown sources.

[//]: # (## Screenshots)

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

- **Fully offline inference.** llama.cpp runs the model on the device's CPU through a JNI bridge.
  Nothing is sent anywhere.
- **Conversations that persist.** Sessions and messages are stored in a local database, featuring
  rename, delete, and a conversation drawer functionality.
- **Streaming replies.** Tokens appear dynamically as they are produced, and generation can be
  interrupted mid-reply, preserving the partial answer.
- **Toggleable thinking process.** Enables or disables model-supported reasoning or "thinking"
  outputs, allowing users to view or hide the model's step-by-step cognitive process depending on
  capability.
- **Mishtir Bhandar, the model shop.** Browse two distinct catalogs: Mishti’s native catalog, which
  is fetched online and cached on the phone, and PocketPal AI's community-tested
  list. Models are evaluated against device memory constraints with personalized recommendations,
  supporting resumable background downloads and SHA-256 integrity verification prior to acceptance.
- **Bring your own model.** Import any GGUF file directly from device storage. Mishti inspects the
  file header, identifying its name, architecture, and quantization parameters before securely
  importing it.
- **Device-aware.** Available RAM is cross-referenced against each model's requirements prior to
  downloading, preventing out-of-memory errors and optimizing bandwidth usage.
- **Tunable.** Configurable parameters including temperature, top-p, top-k, response limits, context
  windows, thread counts, and custom pre-instructions (system prompts), each managed via dedicated
  settings pages.
- **Throughput readout.** Each generated reply provides a real-time performance readout of the
  tokens per second achieved during generation.
- **Material 3 throughout.** Comprehensive support for system, light, and dark themes, dynamic color
  extraction from device wallpapers on Android 12+, four hand-tuned color palettes, and a choice of
  four distinct typefaces.

## Models

Mishti's catalog lives in [`hub/catalog.v1.json`](hub/catalog.v1.json). The app fetches it from
jsDelivr when you open the shop and caches the result for offline browsing. A fresh install needs
a connection to load the list; no catalog is bundled in the app. To add a model:

```bash
python3 tools/hub.py add --id qwen3-0.6b-q4km --name "Qwen3 0.6B" \
    --repo bartowski/Qwen_Qwen3-0.6B-GGUF --file Qwen_Qwen3-0.6B-Q4_K_M.gguf \
    --quant Q4_K_M --description "…" --tags recommended
python3 tools/hub.py validate
```

The script fills in the size, SHA-256, parameter count and context length from Hugging Face.

Weights are fetched from Hugging Face. Each model carries its own upstream license, shown in the
app: Llama 3.2 and Gemma, for example, have their own terms rather than an OSI license.

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
./gradlew :androidApp:assembleDebug    # build the APK
./gradlew :androidApp:installDebug     # build and install on a connected device
./gradlew testAndroidHostTest         # unit tests across all KMP modules
./gradlew :androidApp:lintDebug        # Android Lint
```

The first build compiles ggml and llama.cpp from source and takes a while. Debug builds are
restricted to `arm64-v8a` to keep that cost down; release builds add `x86_64`.

## Building for iOS

Use macOS with Xcode, a JDK compatible with the Gradle wrapper, and CMake 3.22.1 or newer available
on Xcode's PATH (`/opt/homebrew/bin` and `/usr/local/bin` are included by the build phase).
Open `iosApp/iosApp.xcodeproj`, set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`, and select
an arm64 device or Apple Silicon simulator. Xcode builds the CPU llama.cpp backend with
`iosApp/native/build-llama.sh` (also available as the Gradle `:iosApp:buildLlama` task), then builds and embeds the ComposeApp Kotlin framework. Native outputs stay
in Xcode's derived files; no prebuilt native binaries are checked in. The app uses the real native
bridge on both platforms; the scripted engine is confined to tests. The Gradle `:iosApp:assemble`
task links the device and simulator frameworks; building and signing the iOS application itself
requires Xcode. On other hosts, `:composeApp:compileIosMainKotlinMetadata` checks shared iOS metadata
but does not validate native linking or Swift compilation.

Android keeps the native database path and reads the existing native DataStore preferences file.
Early CMP preferences at their former path are retained when present. Exported database schemas and
migration notes are in `core/storage/schemas/`.

## Architecture

Kotlin and Compose Multiplatform target Android and iOS, with MVI for stateful screens and Koin
for dependency injection. Platform adapters live in each module's `androidMain` and `iosMain`
source sets. There is no desktop target.

| Module | Responsibility |
|--------|----------------|
| `:androidApp` | Android host and native llama.cpp build |
| `:iosApp` | Xcode host; Gradle lifecycle tasks for the Kotlin framework |
| `:composeApp` | App shell, drawer, navigation entries and application dependency wiring |
| `:core:domain` | Pure conversation, preferences, theme, inference and document contracts and models |
| `:core:designsystem` | Theme, typography, palettes, fonts, shared icons and common text |
| `:core:device` | Device capabilities and platform clock |
| `:core:llm` | llama.cpp engine implementation and platform bridges |
| `:core:storage` | Room database, migrations, conversation persistence and DataStore |
| `:core:ui` | Generic MVI, navigation, snackbars, Markdown and reusable components |
| `:core:session` | Session list presentation, actions, grouping and title formatting |
| `:core:testing` | Reusable fakes; available only to test source sets |
| `:feature:chat` | Conversation screen and streaming behavior |
| `:feature:model` | Catalogs, downloads, imports and model selection |
| `:feature:settings` | Preferences screens and generated repository documents |
| `:feature:onboarding` | First-run flow and its Lottie animations |
| `:feature:splash` | Startup routing |

Features depend on core modules and never on other features. Core modules never depend on
features or `composeApp`. Domain contracts have no Compose, Room, DataStore or Koin dependencies.
Data implementations depend on those contracts, and `composeApp` selects the implementations.
The model feature's data layer uses storage's installed-model DAO; its domain and UI layers use
its repository interface.

The drawer belongs to the app shell and uses `SessionsViewModel` from `core:session`. Chat
uses the conversation repository from `core:domain`, with Room bound at the app composition root.
Neither chat nor core UI imports the other feature's ViewModel, keeping the dependency graph acyclic.
The `LlmEngine` interface similarly isolates features from the llama.cpp backend.

Resources are owned by the module that uses them. Fonts, palettes and reusable artwork belong to
`core:designsystem`; onboarding animations belong to `feature:onboarding`; Settings owns document
resources. Feature text lives in each feature's `src/commonMain/composeResources/values/strings.xml`.
Cross-module resource access uses explicit generated resource namespaces.

## Tech stack

|            |                                              |
|------------|----------------------------------------------|
| Language   | Kotlin 2.4.20                                |
| UI         | Jetpack Compose (BOM 2026.09.00), Material 3 |
| Navigation | Navigation 3                                 |
| DI         | Koin 4.2                                     |
| Storage    | Room 2.8, DataStore Preferences              |
| Networking | Ktor 3.6                                     |
| Inference  | llama.cpp (`b11577`) via JNI, NDK 28.2       |
| Build      | AGP 9.4, Gradle 9.7, JVM 17 target           |

## Status

Mishti is usable but young. Known limitations, in the interest of not surprising anyone:

- A conversation that outgrows the context window is rejected rather than trimmed.
- Reported tokens per second includes prompt processing, so it under-reports on long chats.
- CPU only — there is no GPU backend.
- Sampling settings are global, not per model.
- Deleting a conversation is confirmed but cannot be undone.

## Privacy

Mishti has no accounts, analytics, ads or crash reporting, and the model runs on your phone, so your
conversations are never sent anywhere. The app goes online only to fetch model catalogs (when you
open Mishtir Bhandar) and to download models from Hugging Face. Its data is left out of Android
backups and device transfers. The draft
[Privacy Policy](docs/privacy-policy.md) lists every request and everything stored on the phone.

## Documentation

The Privacy Policy and Terms of Service are drafts written by the developer. They are not legal
documents and have not been reviewed by a lawyer.

- [About Mishti](docs/about.md)
- [Privacy Policy](docs/privacy-policy.md) (draft)
- [Terms of Service](docs/terms-of-service.md) (draft)
- [Credits](docs/credits.md): llama.cpp, the model catalogs, every library, typeface and model
  license
- [Contributing](CONTRIBUTING.md): report bugs, suggest models, translate, or send code

These pages are also built into the Android and iOS apps (Settings → About). Keep their sources
in `docs/` and `CONTRIBUTING.md` at the repository root. The Settings Gradle `bundleDocs` task stages
them as `files/docs/*.md` in generated Compose resources under `feature/settings/build/`, alongside
Settings string resources. Resource preparation depends on this task, so debug and release builds
include the current documents automatically. No manual copies in source resource directories are
needed. You can run `./gradlew :feature:settings:bundleDocs` to inspect the generated files.

## License

Mishti is released under the [MIT License](LICENSE). Models you download keep their own licenses;
see [Credits](docs/credits.md).

## Credits

Mishti is built on [llama.cpp](https://github.com/ggml-org/llama.cpp) by Georgi Gerganov and
contributors. Its second catalog comes
from [PocketPal AI](https://github.com/a-ghorbani/pocketpal-ai)'s
[pocketpal-device-rules](https://github.com/a-ghorbani/pocketpal-device-rules), fetched unmodified
and never bundled. The typefaces (Inter, Lora, JetBrains Mono) are under the SIL Open Font License.
The full list is in [docs/credits.md](docs/credits.md).
