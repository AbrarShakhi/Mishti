# Privacy Policy (Draft)

> **Draft, not legal advice.** This document is a working draft written by the developer to
> describe how Mishti behaves. It has not been reviewed by a lawyer and is not a legal document.
> It may change before Mishti is published on an app store.

- **Last updated:** 18 November 2026
- **App:** Mishti for Android and iOS
- **Developer:** MD. Shakhiul Abrar

Mishti processes conversations and voice input on your device. This policy explains what the app
stores, what it sends over the network, and why.

## The short version

- Mishti has **no accounts, no servers, no analytics, no advertising and no crash reporting**.
- Mishti's data is **left out of Android backups and device transfers**.
- Chat and dictation run **on your device**. Mishti processes microphone audio in memory, and
  saves a transcript in your conversation history only when you send it as a message.
- Mishti uses the network only to **download models and model catalogs**, and only when you open
  Mishtir Bhandar or start a download.

## What Mishti stores on your phone

| Data                                                                                            | Where                      | Why                                           |
|-------------------------------------------------------------------------------------------------|----------------------------|-----------------------------------------------|
| Conversations: your messages, the replies, titles and timestamps                                | The app's private database | So you can return to a conversation           |
| Settings: theme, colours, typeface, pre-instruction, generation settings, which model is in use | The app's private settings | So your choices persist                       |
| Model files you download or import                                                              | The app's private storage  | So the model can run offline                  |
| Saved copies of the model catalogs                                                              | The app's private storage  | So the shop shows a list without a connection |

Mishti stores this data in its private app storage. When you import a model, Mishti reads the file
you pick in the system file picker and copies it into its own storage, keeping the original intact.

## Voice input and microphone access

When you tap the microphone in chat, Mishti asks for microphone permission if needed. With
permission, it records until you tap stop or the 30-second limit is reached. Leaving chat or
putting the app in the background cancels the active recording. You can change microphone access
in your device settings.

The bundled Whistle model and Cactus runtime process microphone audio in memory on your device.
Audio, live previews and the finished transcript stay on your device during dictation, which
works offline. The transcript goes into your editable draft. Mishti saves it as part of your
conversation only if you send the draft.

## What Mishti sends over the network

Mishti makes network requests only in these cases:

1. **Opening Mishtir Bhandar.** The app checks for an updated version of its own model catalog, at
   most every six hours unless you pull to refresh. The file is requested from
   [jsDelivr](https://www.jsdelivr.com/) (which serves it from Mishti's GitHub repository).
2. **Choosing the PocketPal catalog.** The app requests PocketPal AI's public model list from
   jsDelivr.
3. **Downloading a model.** The model file is downloaded from
   [Hugging Face](https://huggingface.co/) and its content delivery network.
4. **"View on Hugging Face".** Tapping this opens the model's page in your browser.

These requests carry no account, identifier or conversation content. Like any internet request, they
reveal your IP address and basic technical details (such as the app's HTTP client) to the server
that answers them. Those services handle that information under their own policies:

- jsDelivr: <https://www.jsdelivr.com/terms/privacy-policy-jsdelivr-net>
- Hugging Face: <https://huggingface.co/privacy>
- GitHub: <https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement>

Once a chat model is downloaded, chatting works offline. The speech model comes with the app,
so dictation can also run offline from the first use.

## Backups and device transfers

On Android, Mishti excludes its conversations, settings, models and saved catalogs from cloud
backups and device-to-device transfers.

On iOS, your device backup settings determine whether saved app data, including conversations and
settings, is included in iCloud or computer backups.
[Apple's backup guide](https://support.apple.com/en-us/108771)

## Permissions

| Permission                           | Why it is needed                                            |
|--------------------------------------|-------------------------------------------------------------|
| Internet                             | To download models and catalogs                             |
| Foreground service (Android)         | To keep a download or import running when you leave the app |
| Notifications (Android 13 and later) | To show download and import progress. You can decline it.   |
| Microphone (optional)                | To turn speech into an editable chat draft on your device   |

## Deleting your data

- Delete a single conversation from the conversation drawer (long-press, then Delete).
- Delete a model from Mishtir Bhandar → My shelf.
- Remove local app data by clearing Mishti's storage in Android Settings or by deleting the app.
  Manage any existing iOS backups separately in your device or computer backup settings.

Because Mishti has no servers, the developer holds no copy of your data and cannot access, export or
delete it for you.

## Children

Mishti is not directed at children under 13 and does not knowingly collect any information from
anyone.

## Changes to this policy

If this policy changes, the updated version will be published in the
[Mishti repository](https://github.com/AbrarShakhi/Mishti/blob/main/docs/privacy-policy.md)
with a new "Last updated" date.

## Contact

Questions about privacy can be raised at
<https://github.com/AbrarShakhi/Mishti/issues>.
