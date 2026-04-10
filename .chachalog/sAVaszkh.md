---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

Changed error file dumping to be disabled by default. If your workflow relies on error details being written to disk, set `dumpErrorsToFiles=true` in your configuration.
