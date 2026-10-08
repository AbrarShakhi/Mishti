# Privacy Policy (Draft)

> **Draft, not legal advice.** This document is a working draft written by the developer to
> describe how Mishti behaves. It has not been reviewed by a lawyer and is not a legal document.
> It may change before Mishti is published on an app store.

**Last updated:** 8 October 2026
**App:** Mishti for Android (`com.abrarshakhi.mishti`)
**Developer:** MD. Shakhiul Abrar

Mishti is built so that your conversations stay on your phone. This policy explains exactly what
the app stores, what it sends over the network, and why.

## The short version

- Mishti has **no accounts, no servers, no analytics, no advertising and no crash reporting**.
- Mishti's data is **left out of Android backups and device transfers**.
- The language model runs **on your phone**. Your messages and the model's replies are never
  sent anywhere by Mishti.
- Mishti uses the network only to **download models and model catalogs**, and only when you
  open Mistir Bhandar or start a download.

## What Mishti stores on your phone

| Data | Where | Why |
|---|---|---|
| Conversations: your messages, the replies, titles and timestamps | The app's private database | So you can return to a conversation |
| Settings: theme, colours, typeface, pre-instruction, generation settings, which model is in use | The app's private settings | So your choices persist |
| Model files you download or import | The app's private storage | So the model can run offline |
| Saved copies of the model catalogs | The app's private storage | So the shop shows a list without a connection |

This data stays in the app's private storage, which other apps cannot read. Mishti does not
read your contacts, photos, location, microphone, camera or other files. When you import a model,
Mishti reads only the file you pick in the system file picker, copies it into its own storage,
and never changes or deletes the original.

## What Mishti sends over the network

Mishti makes network requests only in these cases:

1. **Opening Mistir Bhandar.** The app checks for an updated version of its own model catalog,
   at most every six hours unless you pull to refresh. The file is requested from
   [jsDelivr](https://www.jsdelivr.com/) (which serves it from Mishti's GitHub repository).
2. **Choosing the PocketPal catalog.** The app requests PocketPal AI's public model list from
   jsDelivr.
3. **Downloading a model.** The model file is downloaded from
   [Hugging Face](https://huggingface.co/) and its content delivery network.
4. **"View on Hugging Face".** Tapping this opens the model's page in your browser.

These requests carry no account, identifier or conversation content. Like any internet request,
they reveal your IP address and basic technical details (such as the app's HTTP client) to the
server that answers them. Those services handle that information under their own policies:

- jsDelivr: <https://www.jsdelivr.com/terms/privacy-policy-jsdelivr-net>
- Hugging Face: <https://huggingface.co/privacy>
- GitHub: <https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement>

Once a model is downloaded, chatting needs no connection at all.

## Android backup and device transfer

Mishti opts out of Android backup. Its conversations, settings, models and saved catalogs are
**not** included in Google cloud backups, and they are **not** copied when you move to a new
phone with Android's device-to-device transfer. Your data exists only on the phone where you
created it, so uninstalling Mishti or losing the phone removes it permanently.

## Permissions

| Permission | Why it is needed |
|---|---|
| Internet | To download models and catalogs |
| Foreground service (data sync) | To keep a download or import running when you leave the app |
| Notifications (Android 13 and later) | To show download and import progress; you can decline it |

## Deleting your data

- Delete a single conversation from the conversation drawer (long-press, then Delete).
- Delete a model from Mistir Bhandar → My shelf.
- Remove everything by clearing Mishti's storage in Android Settings, or by uninstalling the app.

Because Mishti has no servers, the developer holds no copy of your data and cannot access,
export or delete it for you.

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
