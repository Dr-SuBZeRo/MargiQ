# MargiQ v2.2.0

## Changes

- Removed the Product Library and its calculator save/load integration.
- Simplified local persistence to calculation history and user preferences.
- Added a Room migration that removes the obsolete products table while preserving saved calculation history.
- Updated the app version to 2.2.0 (version code 14).

## Compatibility

Android 8.0 (API 26) and newer. Existing installations migrate their local database from version 5 to version 6 on upgrade.
