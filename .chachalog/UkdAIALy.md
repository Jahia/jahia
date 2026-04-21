# Allowed version bumps: patch, minor, major
jahia-private: patch
---

Hardened authorization scopes so that unknown keys in grant blocks are no longer silently ignored, preventing APIs from being unintentionally left open due to misconfigured rules.
