---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

**Deprecation Tracker Service:** fallback to default values and log a warning when an invalid value is set in the OSGi configuration (#4757)

Instead of throwing an exception when an invalid value is provided for a parameter (for example, using a negative value when only positive values are allowed), a warning is now logged in the Jahia logs, and the default value for that parameter is used instead.
