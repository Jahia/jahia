import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.sites.JahiaSitesService

import javax.jcr.RepositoryException

log.info("Starting migration: restrict guest read access to sensitive JCR nodes " +
         "(remove root GRANT, grant /users/guest + /modules + /mounts, " +
         "strip per-site guest grants from /users+/groups and add them to the site node itself)")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {
    Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
        boolean changed = false

        // ── 1. Root j:acl: remove legacy GRANT_u_guest reader ───────────────────────────────────
        //
        // The old root.xml granted reader to u:guest at the repository root, making the entire
        // JCR tree readable by anonymous visitors.  The new root.xml simply removes that entry;
        // no explicit DENY is needed because the root is the top of the inheritance chain.
        // Nodes that require guest access carry their own explicit GRANTs (see steps 2–4).
        JCRNodeWrapper rootNode = session.getRootNode()
        if (rootNode.hasNode("j:acl") && rootNode.getNode("j:acl").hasNode("GRANT_u_guest")) {
            log.info("  Removing legacy GRANT_u_guest ACE from root j:acl")
            rootNode.getNode("j:acl").getNode("GRANT_u_guest").remove()
            changed = true
        } else {
            log.info("  GRANT_u_guest ACE already absent from root j:acl — skipping")
        }

        // ── 2. /users/guest: GRANT reader for guest ─────
        //
        // The DENY on / (step 1) would otherwise prevent the guest user from reading its
        // own node.  This explicit GRANT at /users/guest overrides the parent DENY for that
        // single node.
        JCRNodeWrapper guestUserNode = session.getNode("/users/guest")

        if (guestUserNode.hasNode("j:acl") && guestUserNode.getNode("j:acl").hasNode("GRANT_u_guest")) {
            // Paranoid check: verify the existing ACE actually carries the reader role,
            // and add it if missing (j:roles is multi-valued — use getValues(), not getString()).
            JCRNodeWrapper grantAce = guestUserNode.getNode("j:acl").getNode("GRANT_u_guest")
            List<String> roleList = grantAce.hasProperty("j:roles") ?
                    grantAce.getProperty("j:roles").getValues().collect { it.getString() } : []
            if (!roleList.contains("reader")) {
                log.warn("  GRANT_u_guest ACE on /users/guest is missing role 'reader' (found: {}) — adding it", roleList)
                roleList.add("reader")
                grantAce.setProperty("j:roles", roleList.toArray(new String[0]))
                changed = true
            } else {
                log.info("  GRANT_u_guest reader ACE already present and correct on /users/guest — skipping")
            }
        } else {
            log.info("  Adding GRANT_u_guest reader ACE on /users/guest")
            guestUserNode.grantRoles("u:guest", Collections.singleton("reader"))
            changed = true
        }

        // ── 3. /modules: GRANT reader for guest ───────────────────────────────────────────────────
        //
        // Live rendering requires guest to be able to read /modules for template and resource
        // resolution.  The root DENY (step 1) would block that access without this explicit GRANT.
        JCRNodeWrapper modulesNode = session.getNode("/modules")

        if (modulesNode.hasNode("j:acl") && modulesNode.getNode("j:acl").hasNode("GRANT_u_guest")) {
            // Paranoid check: verify the existing ACE actually carries the reader role,
            // and add it if missing (j:roles is multi-valued — use getValues(), not getString()).
            JCRNodeWrapper grantAce = modulesNode.getNode("j:acl").getNode("GRANT_u_guest")
            List<String> roleList = grantAce.hasProperty("j:roles") ?
                    grantAce.getProperty("j:roles").getValues().collect { it.getString() } : []
            if (!roleList.contains("reader")) {
                log.warn("  GRANT_u_guest ACE on /modules is missing role 'reader' (found: {}) — adding it", roleList)
                roleList.add("reader")
                grantAce.setProperty("j:roles", roleList.toArray(new String[0]))
                changed = true
            } else {
                log.info("  GRANT_u_guest reader ACE already present and correct on /modules — skipping")
            }
        } else {
            log.info("  Adding GRANT_u_guest reader ACE on /modules")
            modulesNode.grantRoles("u:guest", Collections.singleton("reader"))
            changed = true
        }

        // ── 4. /mounts: GRANT reader for guest ────────────────────────────────────────────────────
        //
        // VFS mount points and other external systems mounted under /mounts must remain readable
        // by guest so that file links embedded in live pages continue to resolve correctly.
        JCRNodeWrapper mountsNode = session.getNode("/mounts")

        if (mountsNode.hasNode("j:acl") && mountsNode.getNode("j:acl").hasNode("GRANT_u_guest")) {
            // Paranoid check: verify the existing ACE actually carries the reader role,
            // and add it if missing (j:roles is multi-valued — use getValues(), not getString()).
            JCRNodeWrapper grantAce = mountsNode.getNode("j:acl").getNode("GRANT_u_guest")
            List<String> roleList = grantAce.hasProperty("j:roles") ?
                    grantAce.getProperty("j:roles").getValues().collect { it.getString() } : []
            if (!roleList.contains("reader")) {
                log.warn("  GRANT_u_guest ACE on /mounts is missing role 'reader' (found: {}) — adding it", roleList)
                roleList.add("reader")
                grantAce.setProperty("j:roles", roleList.toArray(new String[0]))
                changed = true
            } else {
                log.info("  GRANT_u_guest reader ACE already present and correct on /mounts — skipping")
            }
        } else {
            log.info("  Adding GRANT_u_guest reader ACE on /mounts")
            mountsNode.grantRoles("u:guest", Collections.singleton("reader"))
            changed = true
        }

        // ── 5. /sites: GRANT reader for guest ─────────────────────────────────────────────────────
        //
        // The new root.xml grants reader to u:guest directly on the /sites folder so that the
        // grant is inherited by every site node, replacing the per-site grant that was previously
        // declared in site.xml.  For existing repositories we add the same ACE here.
        JCRNodeWrapper sitesNode = session.getNode("/sites")

        if (sitesNode.hasNode("j:acl") && sitesNode.getNode("j:acl").hasNode("GRANT_u_guest")) {
            log.info("  GRANT_u_guest reader ACE already present on /sites — skipping")
        } else {
            log.info("  Adding GRANT_u_guest reader ACE on /sites")
            sitesNode.grantRoles("u:guest", Collections.singleton("reader"))
            changed = true
        }

        // ── 6. Per-site changes ────────────────────────────────────────────────────────────────────
        //
        // Previous migration 8.2.4.0-02 added GRANT_u_guest reader on each site's /users and
        // /groups folders so that guest could read site users.  This ticket reverses that grant
        // to prevent anonymous access to site-level user/group data.
        //
        // The GRANT_u_guest on /sites (step 5) now covers live rendering access for all sites by
        // inheritance, so the per-site grant previously added in site.xml is no longer needed.
        // For existing sites that already carry that per-site grant we leave it in place
        // (harmless duplicate of the inherited grant) and only clean up the /users and /groups ACEs.
        //
        // systemsite is treated the same as other sites: some customers use it to share content
        // across multiple sites and guest must retain reader access there too.
        int sitePatched = 0
        int siteSkipped = 0

        JahiaSitesService.getInstance().getSitesNodeList(session).each { siteNode ->
            String siteKey = siteNode.getName()
            boolean siteChanged = false

            log.info("  Processing site: {}", siteKey)

            // 6a. Strip GRANT_u_guest from the per-site /users and /groups ACLs
            ["users", "groups"].each { String folderName ->
                if (siteNode.hasNode(folderName)) {
                    JCRNodeWrapper folder = siteNode.getNode(folderName)
                    if (folder.hasNode("j:acl") && folder.getNode("j:acl").hasNode("GRANT_u_guest")) {
                        log.info("    Removing GRANT_u_guest ACE from /sites/{}/{}/j:acl", siteKey, folderName)
                        folder.getNode("j:acl").getNode("GRANT_u_guest").remove()
                        siteChanged = true
                    } else {
                        log.info("    GRANT_u_guest ACE already absent from /sites/{}/{}/j:acl — skipping", siteKey, folderName)
                    }
                } else {
                    log.debug("    /sites/{}/{} folder not found — skipping", siteKey, folderName)
                }
            }


            if (siteChanged) {
                sitePatched++
            } else {
                siteSkipped++
            }
        }

        if (changed || sitePatched > 0) {
            log.info("Saving changes (global: {}, site(s) patched: {})…", changed, sitePatched)
            session.save()
        }

        log.info("Migration complete: {} site(s) patched, {} site(s) already up-to-date or skipped",
                 sitePatched, siteSkipped)
        return null
    }
})
