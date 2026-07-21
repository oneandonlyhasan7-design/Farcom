# Farcom — changes made in this pass

## I could not actually run a build here
My sandbox has no network egress at all — not even to `services.gradle.org` to download
Gradle itself, let alone `dl.google.com` (AGP/AndroidX) or `download.linphone.org` (the
native Linphone SDK this app links against). I confirmed this directly:
`./gradlew --version` fails at the very first step with a 403 trying to fetch the Gradle
distribution.

So everything below is a careful static analysis + source/config fix pass, validated by:
- Reading every changed file back in full
- XML well-formedness checks (`xmllint`) on every XML file touched, including all 12
  locale `strings.xml` copies
- Manual trace-through of the Gradle Kotlin DSL logic for the branches I changed

**You still need to run the real build** (see "How to build" below) on a machine or CI
runner that has normal internet access, since that's the only way to actually prove it
compiles and links against the native SDK.

## Real bugs fixed

1. **`app/build.gradle.kts` — signing config would break for any fork without the
   original keystore.** `keystoreProperties.load(FileInputStream(...))` ran unconditionally
   with no existence check, and cast values with `as String` with no null-safety. A forked
   or self-hosted deployment that (correctly) `.gitignore`s its keystore secrets, or that's
   missing an entry, would throw `FileNotFoundException`/`ClassCastException` and break
   the build outright, even for debug. Rewrote it to load defensively, fall back to
   unsigned release builds with a clear log message, and never crash.

2. **`google-services.json` was Belledonne Communications' own Firebase project**, hard
   bound to `package_name: org.linphone`. Once the `applicationId` changes (which the
   spec explicitly asks for — see below), the Google Services Gradle plugin would fail
   the build with a "no matching client" error the moment it tried to apply. I moved the
   file to `docs/google-services.json.linphone-original.bak.json` for reference and
   removed it from `app/`. The build script already has a graceful fallback
   (`firebaseCloudMessagingAvailable = googleServices.exists()`), so FCM push and
   Crashlytics now cleanly disable themselves instead of crashing the build. **You'll need
   your own Firebase project + your own `google-services.json` in `app/` if/when you want
   push notifications.**

3. Two locations in `app/src/main/res/xml/network_security_config.xml` were needed but
   missing: targetSdk 37 defaults to blocking all cleartext HTTP, which would silently
   break any HTTP-based feature (e.g. a file-transfer server) pointed at a LAN IP without
   TLS. Added a config allowing cleartext app-wide, with a comment explaining why (LAN
   server addresses are arbitrary/user-chosen, so Android's exact-hostname allow-listing
   can't scope this more narrowly) and what a deployer who wants strict TLS should do
   instead (run a local CA / use a real hostname with a cert).

## Things I could not verify (flagging, not fixing blind)

- **`gradle/libs.versions.toml` pins AGP `9.2.1`, Kotlin `2.3.21`, Gradle `9.5.1`.**
  These are newer than what's in my training data (cutoff Jan 2026), so I can't confirm
  from memory whether they're mutually compatible or contain a typo. I did **not** touch
  them, since guessing "corrected" version numbers without being able to verify against
  current release notes would be worse than leaving them as shipped. First thing to check
  if the real build fails at the plugin-resolution stage: `gradle/libs.versions.toml`,
  cross-checked against the AGP release notes for the actual Gradle version it requires.
- I could not compile-check the Kotlin changes (no `kotlinc` available in this sandbox).
  I traced the logic manually and it's internally consistent, but treat the first real
  `./gradlew assembleDebug` as the actual verification step.

## Hybrid central-server / self-hosted feature

Two things worth knowing up front:

1. **Linphone (and therefore Farcom) already supports connecting to any SIP server —
   domain, IP, or `127.0.0.1` — with zero code changes**, because that's inherent to SIP
   being a decentralized protocol. The existing "third-party SIP account" login screen
   already has free-text domain/transport/port fields. So this wasn't "build hybrid
   connectivity from scratch," it was "make choosing between central and self-hosted a
   clean first-class choice, and remove things that would silently break the self-hosted
   path" (the cleartext issue above being the main one).
2. **All chat history, contacts, call history, and account credentials are already stored
   locally on-device** (Linphone's SDK uses its own local SQLite storage) — a central
   server, if used at all, is only ever involved in SIP registration/routing/presence,
   never as a place your data lives. That's the existing architecture, not something
   this pass added.

What I actually added:

- **`CorePreferences.kt`**: new preferences —
  `farcomCentralServerDomain`, `farcomCentralServerDefaultTransport`,
  `farcomSelfHostedDefaultTransport`, `farcomSelfHostedDefaultAddress`,
  `farcomLastSelectedServerMode` — all backed by the existing `linphonerc` config
  mechanism (same pattern Linphone already uses for every other default), so a deployer
  configures them in `assets/linphonerc_default` with **zero source changes**.
- **`assets/linphonerc_default`**: new `[app]` keys with sane defaults — central domain
  is blank by default (ships with self-hosted-only unless you set it), self-hosted
  address defaults to `127.0.0.1`, self-hosted transport defaults to UDP (typical for
  LAN), central transport defaults to TLS (typical for Internet).
- **`ThirdPartySipAccountLoginViewModel.kt`**: two new methods, `useCentralServer()` and
  `useSelfHostedServer()`, that pre-fill the domain + transport fields from those
  preferences and remember the user's last choice. Doesn't touch username/password, and
  never overwrites a domain the user already typed in unless it's still the default.
- **`assistant_third_party_sip_account_login_fragment.xml`**: two small buttons —
  "Central server" / "Self-hosted / Local" — added above the existing domain field,
  wired directly to those view-model methods via data binding.
- **New strings**: `farcom_connection_mode_label`, `farcom_use_central_server`,
  `farcom_use_self_hosted_server` in `values/strings.xml`.

### To point this at your own central server
Edit one file, `app/src/main/assets/linphonerc_default`:
```
farcom_central_server_domain=farcom.yourdomain.org
```
Nothing else needs to change. Leave it blank to ship self-hosted-only.

### To connect a device to a local/self-hosted server
On the login screen, tap **Self-hosted / Local** — this pre-fills `127.0.0.1`, editable
to any LAN IP (e.g. `192.168.1.50`) or local hostname, with UDP transport by default.

## Branding changes (scoped, not a blind global search-replace)

- `<!ENTITY appName>` changed from `"Linphone"` to `"Farcom"` in all 13 copies of
  `strings.xml` (one per locale) — this is the single hook Linphone's own build already
  designed for app-name rebranding, so every place `app_name` is referenced picks it up
  automatically.
- `applicationId` decoupled from the internal Kotlin/data-binding `namespace`.
  `namespace` stays `"org.linphone"` (deliberately **not** changed — renaming it means
  rewriting every `R`/data-binding reference across ~3MB of Kotlin, which is exactly the
  "blind search-and-replace that breaks dependencies" the spec says to avoid, and it
  would make pulling in future upstream Linphone changes far harder). `applicationId` is
  now `org.farcom.app` — this is what actually controls the Play Store identity /
  install-alongside-Linphone behavior, and it's safe to change independently.
- APK output filename changed from `linphone-android-*.apk` to `farcom-android-*.apk`.
- **Not yet done** (needs real assets, not something I can generate blind): launcher
  icons, splash screen, color palette (`values/colors.xml` / theme). The spec's "replace
  icons / splash / colors" needs actual design assets — happy to help wire in whatever
  you provide, or rough out a placeholder palette if you want one now.

## How to build (on a machine/CI with real internet access)

```bash
cd linphone-android-6.2.3
./gradlew assembleDebug
```

First real build will take a while — it fetches the prebuilt `linphone-sdk-android` AAR
(~hundreds of MB, native C++ libs for multiple ABIs) from Linphone's Maven repo the first
time. Subsequent builds are much faster.

If you hit a build error, send me the exact Gradle error output and I'll fix the root
cause — that's the one thing I genuinely cannot pre-verify from this environment.
