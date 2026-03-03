---
description: Instructions for reviewing changelog notes as they are being added to Pull Requests. Based upon the style guide available here https://jahia-confluence.atlassian.net/wiki/spaces/PR/pages/2065630/Changelog+Notes+style+guide
applyTo: '.chachalog/**'
---

# Changelog Notes Writing Instructions

Changelog Notes are short, customer-facing descriptions of changes included in a release.

Changelog are written in the .chachalog/ folder at the root of the repository. Changelog are not mandatory and the review should only be performed if a file was modified in this folder.

They are written so that someone familiar with the product (but not with its internal implementation details) can understand:
- what changed or was fixed
- whether they are affected
- what they may need to do (especially for breaking changes)

## Core principles

### Keep it short
- Start with a single-line summary (≤ 120 characters).
- Add 1–2 sentences only if needed for clarity (who is affected, conditions, what to do).

### Be clear
- Use product language and user-visible symptoms.
- Avoid internal jargon and engineering terms.

### Front-load information
- Put the most important information at the start of the sentence.

### Prefer outcomes over implementation
- Focus on the user experiences, not how we built/fixed it.

## Format
- Write changelog entries as plain sentences.
- Do not use structured prefixes such as scope/category labels (for now).

## Recommended sentence patterns

### Start with a strong verb (past tense)
Use past tense verbs and describe the new behavior as a statement of fact:
- Fixed …
- Added …
- Improved …
- Changed …
- Deprecated …
- Removed …
- Secured … / Hardened …

Optional: add a second sentence when needed for conditions, scope, or user action.

Prefer **Fixed** over “Corrected”, and **Improved** over “Made better”.

### Examples

#### ✅ Do
- Fixed locked content so it can no longer be marked as Work in Progress.
- Fixed startup so it no longer generates error logs when no configuration file is present.

#### 🚫 Don’t
- “Fixed startup” (too vague)
- “Fixed method in MissingModulesValidator to return successful validation…” (internal details)

## Breaking changes

A breaking change is something that might require customers to update their configuration, code, or workflow when upgrading.

For breaking changes, always write an entry that is longer and more detailed than usual so customers can:
- quickly check whether the breaking change affects them
- know exactly what actions to take if it does

When documenting a breaking change:
1. Clearly state what was changed and why.
2. Explain who is affected and how they can check if they’re impacted.
3. If the change only affects certain features or setups, say so.
4. Explain, step by step, what customers should do if they are impacted so they can adopt the new version.

## Words and patterns to avoid

### Avoid vague or non-user-facing wording
Avoid terms like:
- “various”, “related”, “some”, “stuff”

Avoid engineering-only descriptions like:
- “refactor”, “cleanup”, “restructured” (unless user behavior changed)
- internal names (classes, methods, services, module names, exceptions)
- “performance improvements” without stating what the user will notice

### Avoid engineering nouns when a user-facing term exists
Avoid:
- “mechanism”, “pipeline”, “framework”, “handler”, “validator”

Avoid “Added a mechanism…”.
Prefer user-facing nouns.

#### ✅ Better alternatives
- Added CSV export for results to simplify reporting.
- Added the ability to export results as CSV to simplify reporting.

#### 🚫 Don’t
- Added a mechanism to export results as CSV to simplify reporting.

## Bug fixes
Describe:
- the user-visible symptom
- (optional) the conditions under which it occurred (when/where/for whom)

Ensure that non-developers can understand what was fixed.

### ✅ Do
- Fixed undo history so it no longer breaks after renaming a file.
- Fixed spurious startup error logs when no configuration file is present.

### 🚫 Don’t
- “Fixed import validation related issues.” (too vague)
- Internal details: “Fixed method in MissingModulesValidator…”

## Enhancements and new features
State the capability and (briefly) the benefit.

### ✅ Do
- Added CSV export for results to simplify reporting.
- Improved search results so they now show the matching field to reduce guesswork.

### 🚫 Don’t
- “Improved performance” (without saying what changed for the user)
- “Implemented feature X” (engineering phrasing)