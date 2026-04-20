---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

- Deprecated legacy `/cms/*` Spring MVC endpoints (no functional removal).
- Deprecated direct `DELETE`/`PUT`/`POST` on resource URLs without explicit actions.
- Deprecated WebDAV and JCR remoting stack.
- Deprecated unused/legacy areas (preferences layer, Studio/Git/SVN/Maven integration paths, migration/GWT utilities).
- Removed `commons-id` and HttpClient 4 usages from Jahia core.
- Added Maven Checkstyle guardrails to block new `commons-id` / HttpClient 4 usage (libs kept for backward compatibility).
- Expanded system export-package deprecation tracking (including 150+ leaked third-party packages).
- **No breaking changes**: runtime behavior preserved; this wave is deprecation signaling and adoption guidance.
