# Publishing OpenTune Lite

## How a release happens

The release workflow builds the signed APKs and attaches them to a GitHub
release. It never publishes to a store and never touches the website.

To release: bump `versionCode` and `versionName` in `app/build.gradle.kts`,
merge to `main`, and the release follows. The release notes come from
`docs/releases/<version>.md` when that file exists.

Signing needs the repository Actions secrets `OPENTUNE_KEYSTORE_BASE64`,
`OPENTUNE_KEYSTORE_PASSWORD`, `OPENTUNE_KEY_ALIAS` and `OPENTUNE_KEY_PASSWORD`.
Missing signing material stops the release; debug-signed APKs are never
published. Never substitute a new signing key for an already-distributed app.

## Where people get it

| Channel | What to use | Remaining step |
| --- | --- | --- |
| GitHub Releases | Signed APKs, source repository and stable version tags | Publish an approved non-prerelease GitHub release. |
| Obtainium | `https://github.com/grayguava/opentune-lite` | Add the repository as a GitHub source in Obtainium. Stable signed APK releases are already the supported format. |
| Your own F-Droid-compatible repository | Existing signed APKs | Use `fdroid init`, place APKs in its `repo/` folder, then `fdroid update`. Publish the signed repository using HTTPS and retain the repository signing key for future indexes. See https://f-droid.org/docs/Setup_an_F-Droid_App_Repo/. |

There is no store listing and no Play bundle in this build.

## Release notes in the app

The app's "What's new" sheet reads the notes in
`app/src/main/java/com/grayguava/opentune/lite/ui/components/WhatsNew.kt`. Add the coming
version's notes at the top of that list when releasing. The sheet shows
nothing new for a version with no notes of its own.

## Website

Canonical app page: https://grayguava.github.io/opentune-lite/

Privacy policy: https://grayguava.github.io/opentune-lite/privacy/

The website includes canonical URLs, app structured data, crawlable FAQs and
`sitemap.xml`. Once published, submit
`https://grayguava.github.io/opentune-lite/sitemap.xml` in Google Search
Console and Bing Webmaster Tools after verifying the site. Indexing and
ranking are controlled by search engines. A project-level
`/opentune-lite/robots.txt` would not govern this host: robots.txt belongs at
`https://grayguava.github.io/robots.txt`, so this project uses page-level
robots metadata and omits private URLs from the sitemap.
