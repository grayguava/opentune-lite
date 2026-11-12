# Service integrations in OpenTune builds

## Last.fm

Listeners sign in using a username and password. The app exchanges those directly
with Last.fm for a session key and does not store the password. Existing connected
accounts retain their signing credentials and queued scrobbles across updates.

The build owner must register **OpenTune's own application** at
https://www.last.fm/api/account/create and configure the repository Actions secrets
`LASTFM_API_KEY` and `LASTFM_API_SECRET`. The preview and release workflows pass
these into the Android core module. Local builds accept the same environment names.
Without a configured application, new sign-in is disabled with an explanation;
there is no pretend connection and users are not asked to register their own API app.

BitChord supplies application credentials in its code. That does not make Last.fm
registration optional. OpenTune does not reuse another app's credentials. Credentials
embedded in mobile binaries can be recovered; Actions secrets keep them out of Git
and build logs, not out of distributed binaries.

## Discord presence

Discord titles a presence card with the registered name of the Discord application
it comes from, so OpenTune needs its own application, named OpenTune. Earlier
desktop and extension builds borrowed BitChord's application id, which is why the
card read "Listening to BitChord". That id is gone; no BitChord application
identity is used now.

To set it up once:

1. At [discord.com/developers/applications](https://discord.com/developers/applications),
   create an application named **OpenTune** and give it the OpenTune icon
   (`docs/logo.png`).
2. Copy its **Application ID** (a 17 to 20 digit number; it isn't a secret).
3. In the repository's **Settings → Secrets and variables → Actions → Variables**,
   add a variable `DISCORD_APP_ID` with that number (a secret of that name works too). CI and the
   release build read it.

The Android app has no Discord presence yet. BitChord's Android presence uses a
Discord user-token gateway, not a registration-free OAuth integration; OpenTune
would need a tested transport of its own before offering it there.
