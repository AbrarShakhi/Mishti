# About Mishti

**Mishti** (মিষ্টি) means *sweet* in Bengali. It is an Android app that runs a small language model
entirely on your phone, so you can chat with an AI without sending a word anywhere.

## Why Mishti exists

Most AI assistants send every message to a server. Mishti takes the other route: the model is a file
on your phone, and [llama.cpp](https://github.com/ggml-org/llama.cpp) runs it on the phone's own
processor. Once a model is downloaded, Mishti works in airplane mode. There is no account, no server
and no telemetry.

## What it does

- **Private chat.** Conversations are stored only on your phone, with a drawer to rename, revisit
  and delete them. Replies stream in as they are written, and you can stop a reply mid-way.
- **Mishtir Bhandar, the sweet shop.** *Mishtir Bhandar* (মিষ্টির ভাণ্ডার) means a store of sweets. It
  is where models live:
    - **My shelf** holds the models you have downloaded or imported, and the one you are chatting
      with.
    - **Browse** offers two catalogs: Mishti's own hand-picked list, which ships inside the app, and
      the community-tested list from PocketPal AI.
    - Every model is marked by whether it fits your phone's memory, and the best picks for your
      phone are recommended first.
    - **Import** lets you bring any GGUF model file from your phone's storage.
- **Make it yours.** Light, dark or system theme; wallpaper colors on Android 12 and later or one of
  four palettes; a choice of typefaces; and a pre-instruction that sets the assistant's tone.
- **For tinkerers.** Temperature, top-p, top-k, reply length, context window and thread count, each
  on its own settings page.

## Requirements

- Android 11 or newer
- An `arm64-v8a` or `x86_64` processor
- At least 3.2 GB of memory, and more for larger models
- Free storage for the models you download

## Open source

Mishti is free and open source under the [MIT License](../LICENSE).

- Source code: <https://github.com/AbrarShakhi/Mishti>
- Releases: <https://github.com/AbrarShakhi/Mishti/releases>
- Report a problem: <https://github.com/AbrarShakhi/Mishti/issues>

## Made by

MD. Shakhiul Abrar.

See also the [Credits](credits.md), and the draft [Privacy Policy](privacy-policy.md) and
[Terms of Service](terms-of-service.md).
