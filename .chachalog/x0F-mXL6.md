---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

Introduce `IOResource` API — Decouple public services from Spring Resource (#4714)

New `org.jahia.api.io.IOResource` interface replaces `org.springframework.core.io.Resource` across all OSGi-exported API signatures, replacing Spring framework types from Jahia's public API surface.

Five implementations cover all existing resource patterns: `FileSystemIOResource`, `UrlIOResource`, `BundleIOResource`, `InputStreamIOResource`, and `ByteArrayIOResource`.

All previous methods accepting or returning Spring `Resource` are deprecated since 8.2.4.0 (removal targeted for 8.3). 
Binary compatibility is fully preserved, existing compiled modules continue to work without recompilation.
