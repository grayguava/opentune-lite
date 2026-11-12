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
