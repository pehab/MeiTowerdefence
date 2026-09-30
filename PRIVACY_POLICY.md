# Privacy Policy – MeiTowerDefense

MeiTowerDefense is an Android tower-defense game.

## Data collection and use
MeiTowerDefense does not use advertising or analytics and does not sell personal data.
Most game progress and settings are stored locally on the user's device.

The application uses Firebase Crashlytics to receive technical crash reports and diagnose
app errors. Crash reports may contain device and operating-system information, app version
and state, stack traces, diagnostic identifiers and related technical data. Crashlytics is
not used for advertising or analytics.

## Public endless-mode leaderboard
Publishing an endless-mode record is optional. The app asks before every newly eligible
personal record is submitted. If the player agrees, the following data is stored in Google
Cloud Firestore and displayed publicly in the in-app leaderboard:

- the freely chosen public display name;
- the number of fully completed endless waves (the score);
- the server time of the latest published personal record;
- a Firebase anonymous account identifier used internally to keep one leaderboard row per
  app identity. The identifier is not shown in the leaderboard.

The app does not upload campaign progress, stars, achievements, email addresses or other
locally stored game data to the leaderboard. A later, higher published score replaces the
previous score for the same anonymous account. Choosing not to publish has no effect on the
locally saved personal record.

Players should not use their real name if they do not want it displayed publicly.

## Local data
Stars, upgrades, level ratings, unlocks, achievement progress, kill counters, endless
records and the last leaderboard display name remain on the device. They are not
synchronized to a MeiTowerDefense account or cloud save.

The in-app "Gesamten Fortschritt zurücksetzen" action resets local game data and both local
recovery copies. It also attempts to delete the leaderboard entry associated with the
current anonymous Firebase identity. If the device is offline or Firebase cannot be
reached, the app reports that the online deletion must be retried.

## Third parties
MeiTowerDefense uses these Google Firebase services:

- Firebase Crashlytics for crash reporting;
- Firebase Authentication for an anonymous leaderboard identity;
- Cloud Firestore for published leaderboard entries.

Google may process the technical and leaderboard data required to provide these services.
See:

- Google Privacy Policy: https://policies.google.com/privacy
- Firebase Privacy and Security: https://firebase.google.com/support/privacy/

## Data sharing
Published leaderboard names and scores are visible to other app users. Technical crash
information and leaderboard data are processed by Google/Firebase as described above.
Data is not sold.

## Data deletion
Local data can be removed with the in-app full reset, by clearing app data, or by
uninstalling the application. The in-app reset also deletes the published leaderboard
entry while the anonymous identity is still available. After reinstalling the app or
clearing its app data, that earlier anonymous identity can no longer be matched locally.
For deletion or correction of such an entry, contact the developer and include the
displayed name and score so the entry can be located.

## Security
Firestore security rules restrict leaderboard writes to the player's anonymous Firebase
identity, accept only the name/score/timestamp schema, and only allow a score to increase.
No client-only leaderboard can fully prevent scores submitted by a modified app.
Reasonable technical measures are used to protect data, but no method of electronic storage
or transmission can guarantee absolute security.

## Children
MeiTowerDefense is not specifically directed at children. A public leaderboard name should
not contain personal contact information.

## Changes
This privacy policy may be updated when the application's functionality or services change.
The current version is published in this repository.

## Contact
Questions and requests regarding privacy or leaderboard entries can be sent to:

Email: phaberland@googlemail.com  
GitHub: https://github.com/pehab/MeiTowerdefence
