The shared database uses version 5 to unify the native Android and early CMP histories.

Versions 1–4 are the exported native Android schemas. Version 5 preserves the version 4 tables and identity hash. Its migrations also accept the early CMP version 1 database, which already contained the token speed and reasoning columns but lacked `installed_models`.

Keep `mishti.db` at the existing Android database path. Migrations preserve chat history and installed models; unsupported upgrades or downgrades fail instead of erasing user data. Room exports new schemas here through the `core:storage` module's KSP tasks.
