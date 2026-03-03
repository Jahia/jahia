---
# Allowed version bumps: patch, minor, major
jahia-private: patch
---

**URL rewriting**: Fixed `srcset` attribute rewriting to handle substring URLs correctly

Fixed a bug in `UrlRewriteVisitor` where URLs in `srcset` attributes could be incorrectly rewritten when one URL was a substring of another. The issue occurred with simple string replacement, which would replace partial matches within longer URLs.

For example, with this `srcset`:
```html
<img srcset="/files/sample.png?w=100 100w, /files/sample.png 200w">
```

The previous implementation using `StringUtils.replace()` would incorrectly replace `/files/sample.png` within `/files/sample.png?w=100`, potentially causing double replacements or malformed URLs.

The fix uses a three-step approach:
1. **Parse**: Split the `srcset` into individual entries using the new `SrcSetURLReplacer.parseSrcsetEntries()` method, which extracts both the URL and descriptor (width or pixel density) for each entry.
2. **Rewrite**: Rewrite each URL individually while preserving its descriptor.
3. **Rebuild**: Reconstruct the `srcset` string by joining the rewritten entries with comma separators.

This eliminates the substring collision issue entirely by working with structured data instead of string replacement.

