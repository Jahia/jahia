---
# Allowed version bumps: patch, minor, major
jahia-private: minor
---
When an area was defined at the template level and the `areaAsSubNode` option was enabled, any content nodes part of the template placed under that area were silently ignored and not rendered. Content is now correctly resolved and rendered in all cases. (#4919)
