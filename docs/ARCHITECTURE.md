# Architecture Overview

NuneTV is organized into modular layers to keep IPTV parsing, provider management, and Leanback UI responsibilities isolated.

## Data & Network

- **`data/M3uParser`** – Parses M3U playlists into strongly-typed `Channel` objects with channel grouping metadata.
- **`data/EpgParser`** – Uses a SAX parser to translate XMLTV data into `EpgProgram` entries.
- **`network/XStreamCodesService`** – Wraps the XStream Codes API for login and fetching live, VOD, and series catalogues.
- **`data/ProviderStorage`** – Persists provider credentials with `EncryptedSharedPreferences` to keep sensitive data at rest.

## Domain

- **`repository/IptvRepository`** – Orchestrates API calls, playlist downloads, and EPG parsing while returning rich `IptvContent` objects.
- **`viewmodels/MainViewModel`** – Coordinates provider selection, loads catalogues, and exposes Leanback-friendly UI state.
- **`viewmodels/SettingsViewModel`** – Owns CRUD operations on provider credentials and exposes the active provider signal to the UI.

## UI

- **Activities**
  - `MainActivity` hosts the Leanback browsing experience and bridges provider selection and playback.
  - `SettingsActivity` provides a remote-friendly form for managing multiple IPTV providers.
  - `PlayerActivity` hosts the ExoPlayer-driven playback surface with buffering and error handling feedback.
- **Fragments**
  - `MainBrowseFragment` renders the Leanback rows for Live TV, Movies, Series, and Favorites.
  - `ChannelDetailsFragment` supplies guided actions for playback, favorites, and EPG access.
  - `EpgGridFragment` displays channel-specific programme guides.
  - `ChannelSearchFragment` filters the aggregated channel catalogue.

## Playback

ExoPlayer is wrapped by `utils/PlayerManager`, which configures HTTP data sources and exposes a reusable player instance. `PlayerActivity` attaches the player to a `LeanbackPlayerView` to support TV remote controls.

## Provider Workflow

1. Credentials are encrypted via `ProviderStorage` and flagged as active.
2. `MainViewModel` loads the active provider, authenticates against XStream Codes, merges optional M3U playlists, and fetches EPG data.
3. UI fragments observe `MainViewModel` state to refresh Leanback rows and EPG overlays.

This separation allows IPTV integrations to evolve independently of Leanback UI concerns and makes it straightforward to add future providers or replace the streaming backend.
