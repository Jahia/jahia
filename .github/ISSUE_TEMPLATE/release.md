---
name: Release
about: Create a release
title: ''
type: Release
projects: ["Jahia/29"]

---

## :white_check_mark: Summary

1. [Minimum Jahia version](#user-content-minimum-jahia-version)
2. [Testing matrix](#user-content-testing-matrix)
3. [Prepare checklist](#user-content-prepare-checklist)
4. [Tests checklist](#user-content-test-checklist)
5. [Publication checklist](#user-content-publication-checklist)

<h2 id="minimum-jahia-version">Minimum Jahia version</h2>

> We aim at reducing the possible deployment matrices by regularly bumping up the minimum Jahia version when releasing new versions of our modules.
When creating a Release ticket for a module, consult with your PM to identify the new minimum Jahia version this module must be associated with.
The default guideline is for a new module release to be compatible with the 2 previous releases.

Current Jahia Version: 8.X.X.X (version currently in the POM)
Desired Jahia Version: 8.X.X.X

<h2 id="testing-matrix">:scroll: Testing matrix</h2>

> We aim at clearly documenting the possible deployment scenarios in a matrix and specify which ones are expected to be tested or not.
In the testing matrix, always use the latest patch version of a particular release

|To Test (YES/NO)|Jahia Version|Dependency A|Dependency B|PASS / FAIL|
|---|---|---|---|---|
|YES / NO|8.1.1.2|1.0.0|3.0.0| |
|YES / NO|8.1.7.1|2.0.0|3.0.0| |

:information_source: If you are releasing for the main branch of a module, make sure to complete the checklist below when working on the ticket.

<h2 id="prepare-checklist">:pencil2: Prepare checklist</h2>

- [ ] All other tickets with that fixVersion are closed
- [ ] Testing matrix and Minimum Jahia version are detailed
- [ ] Jahia-parent (minimum Jahia version) was updated if requested in the ticket
- [ ] The release workflow will [publish](https://github.com/Jahia/sandbox/commit/9a6ab10c8fbe5e11eb7fe3c6b391be2be776319b) a slack notification
- [ ] Release creation has been triggered from the Github Release UI (https://github.com/Jahia/[module-repo]/releases/new)

<h2 id="tests-checklist">:vertical_traffic_light: Tests checklist</h2>

General
- [ ] Manual tests detailing testing steps for validating the release of this module are present on Testrail
- [ ] Automated tests using the release artifacts were executed

Module migration
- [ ] Upgrade from the previous released version of the module was tested
- [ ] The upgrade did not require clearing the browser cache (i.e. missing labels)

While Testing
- [ ] No warnings or errors are present in the browser console when testing the module
- [ ] No warnings or errors are present in Jahia logs when testing the module (incl. migration)
- [ ] Verified tickets present in the release are actually included in the release artifact

After Testing
- [ ] Tested combinations (Jahia versions, modules versions) are listed in this released ticket
- [ ] Tested scenarios not detailed in Testrail are listed in this release ticket
- [ ] The version was updated in the [Selenium integration tests](https://github.com/Jahia/jahia-qa/blob/f4f788d56fd624174302231e3d64878cd343e515/pom.xml#L75)

<h2 id="publication-checklist">:rocket: Publication checklist</h2>

- [ ] The release changelog was prepared
- [ ] In case of a major release, a **Breaking Changes** section is present in the Changelog
- [ ] The artifact was released on Nexus
- [ ] If applicable, corresponding academy pages were published
- [ ] The module was published on the store
- [ ] The [modules releases log page](https://edit.jahia.com/jahia/page-composer/default/en/sites/academy/home/customer-center/release-lifecycle-1.html) on the Academy was updated
- [ ] A message was published on slack #releases channel
