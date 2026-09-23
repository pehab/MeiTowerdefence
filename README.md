# MeiTowerDefense

Android tower defense, Kotlin, Cursed-Treasure-style dual-currency progression. Built as
an Android Studio project; has never been compiled (no Android SDK / Google Maven access
in the environment this was built in) - see "What's verified" below for exactly what has
and hasn't been checked.

## Architecture

- **Gameplay canvas**: classic `SurfaceView` + a dedicated `GameThread` (fixed-role game
  loop: tick, then render, repeat), *not* Jetpack Compose - real-time rendering with many
  simultaneously moving enemies/projectiles is not what Compose's recomposition model is
  built for. See `engine/GameThread.kt` and `view/GameSurfaceView.kt`.
- **Simulation**: `sim/GameSimulator.kt` is the entire game tick (spawning, movement,
  targeting, combat, win/lose) as pure functions - `GameSession` in, `GameSession` out,
  no Android imports anywhere in `model/` or `sim/`. That's deliberate: it's what makes
  the whole simulation layer unit-testable on the plain JVM, and it's genuinely been run
  that way in this environment (see below).
- **Menus, level select, star shop, in-game HUD**: Jetpack Compose. `ui/GameController.kt`
  is the thread-safe bridge between GameThread (background thread, owns the authoritative
  `GameSession`) and touch input / the Compose HUD (main thread) - see its class doc for
  why an action queue, not shared mutable state.
- **Persistence**: a single JSON save file (`save/`), not Room - the save-game data
  (stars, meta-upgrade levels, per-level best-stars) is flat with no relations or
  queries beyond "load everything, save everything".

## The two currencies

- **In-level gold**: earned from kills, spent building and upgrading towers within one
  level's playthrough. Towers level 1→5; reaching level 3 requires picking one of two
  specialization branches (`model/Specialization.kt`), continuing to level 5 within that
  branch. Resets every level.
- **Stars**: earned from a level's 0-3 star rating (`model/LevelRating.kt` - based on
  lives remaining at the end), spent in the star shop on permanent, account-wide
  upgrades (`model/MetaUpgradeType.kt`): more gold income, more starting gold/lives per
  level, and per-tower-type passive bonuses (e.g. Fire towers' splash radius, Ice towers'
  slow/freeze duration) that stack on top of whatever specialization was chosen that game.

## Towers and enemies (v1 roster)

4 tower types (Archer, Cannon, Fire, Ice), each with 2 specialization branches -
`model/TowerType.kt` / `model/Specialization.kt`. 5 enemy types (Basic, Fast, Armored,
Flying, Boss) - `model/EnemyType.kt`. Archer and Ice can hit flying enemies; Cannon and
Fire cannot, by design - a level with a flying wave needs the right tower mix, not just
"more towers". Healers and spawners are explicitly deferred to a later pass.

## Levels

3 hand-authored levels (`content/LevelCatalog.kt`), ramping which enemy types appear:
Waldpfad (Basic/Fast only), Bergpass (adds Armored), Talkessel (adds Flying + a Boss
finale). Endless mode is **not implemented** - `LevelCatalog.byId("endless")` returns
null and `GameActivity` just closes rather than crashing; the level-select screen's
Endless card is unreachable in practice until all 3 real levels are beaten anyway.

## What's verified vs. not

The environment this was built in has no Android SDK and no access to Google's Maven
repository, so nothing touching `android.*`, Jetpack Compose, or AndroidX could actually
be compiled here. What *was* compiled and run, with a real Kotlin 2.1.0 compiler (the
project targets 2.4.20 - close enough for everything used here) and JUnit:

- All of `model/` and `sim/` (the entire simulation) - 32 tests, including build/upgrade/
  sell validation, splash damage, ground-vs-air targeting, slow/freeze effects, win/lose
  conditions, and determinism under a fixed seed.
- `render/GameCamera.kt` (grid↔pixel coordinate mapping) - 4 tests.
- All 3 levels, played out by a simple greedy bot, to sanity-check they're actually
  winnable with a reasonable defense (this caught two real issues along the way: an
  unused air path eating buildable space, and Waldpfad - meant to be the easy first
  level - originally being harder than Bergpass).

Not locally verifiable, and so only manually reviewed rather than compiler-checked:
`render/GameRenderer.kt` (needs `android.graphics`), everything in `view/` and `ui/`
(needs AndroidX/Compose/Navigation/kotlinx-coroutines-test), and `save/` (needs
`kotlinx.serialization`). A real Gradle sync in Android Studio is the first actual
compile these get.

## Known open items

- No app icon yet (uses a placeholder system drawable).
- All UI text is hardcoded German - no string-resource extraction yet (same starting
  point MeiOCRWorkout had before that pass; happy to do the same here if wanted).
- Endless mode is unimplemented (see above).
- Numbers (costs, damage, wave pacing) have only been sanity-checked by a simple bot,
  not real playtesting - expect to want to retune after actually playing it.
