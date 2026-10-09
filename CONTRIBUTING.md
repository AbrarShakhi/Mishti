# Contributing to Mishti

Thank you for wanting to make Mishti sweeter. Every kind of help counts: reporting a bug, suggesting
a model, translating the app, or sending code.

## Ways to help

### Report a bug

Open a [bug report](https://github.com/AbrarShakhi/Mishti/issues/new?template=bug-report.yml).
Please include your phone model, Android version, Mishti version (Settings → About Mishti), the
model you were using, and the steps that lead to the problem. Screenshots help a lot.

### Suggest a model

Open
a [model suggestion](https://github.com/AbrarShakhi/Mishti/issues/new?template=model-suggestion.yml)
with the Hugging Face repository and the GGUF file you have in mind. Small, chat-tuned models (under
about 4 GB) that you have tried on a phone are the most useful. If you are comfortable with a pull
request, you can add it yourself:

```bash
python3 tools/hub.py add --id <id> --name "<Name>" --repo <owner/repo-GGUF> \
    --file <file.gguf> --quant Q4_K_M --description "<one friendly sentence>"
python3 tools/hub.py validate
```

The script fills in the size, SHA-256 and the rest from Hugging Face. Never change the ids of
existing entries: phones that already downloaded a model find it by its id.

### Translate Mishti

All of Mishti's text lives in `app/src/main/res/values/strings.xml`. To add a language, copy that
file to `values-<language code>/strings.xml` (for example `values-bn` for Bengali), translate the
text, and leave the names and placeholders (`%1$s`, `%1$d`) as they are.

### Improve the code

1. Fork the repository and clone it with its submodule:
   ```bash
   git clone --recurse-submodules https://github.com/<you>/Mishti.git
   ```
2. Install the Android SDK with NDK `28.2.13676358` and CMake `3.22.1`, and point
   `local.properties` at it. The first build compiles llama.cpp and takes a while.
3. Make your change on a branch, then check it:
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```
4. Open a pull request that explains what changed and why, with screenshots for anything visible.

## House style

- Kotlin with Jetpack Compose and Material 3 Expressive. Follow the structure described in
  [CLAUDE.md](https://github.com/AbrarShakhi/Mishti/blob/main/CLAUDE.md): MVI per screen, one
  feature per folder.
- **No comments in source files.** Let names and structure explain the code, and put any non-obvious
  reasoning in the pull request or commit message.
- User-visible text goes in `strings.xml`, never inline in Kotlin.
- Add or update unit tests for behavior you change. Test names are sentences in backticks.

## Be kind

Be respectful and patient with everyone. Assume good intent, and help newcomers find their way.

By contributing, you agree that your contribution is released under the project's
[MIT License](https://github.com/AbrarShakhi/Mishti/blob/main/LICENSE).
