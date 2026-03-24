---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

Refactor ETag / Gzip / Range HTTP headers in core servlets (#4854)

Introduce proper variant-aware ETags, servlet-level gzip, and range request support for 3 core servlets: `ResourceServlet`, `FileServlet` and `StaticFileServlet`.
