import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.sites.JahiaSitesService

import javax.jcr.RepositoryException

log.info("Starting migration: add protective ACLs (j:inherit=false + GRANT site-administrator, " +
         "reader for guest/users, privileged for site-privileged) on /users and /groups for all existing sites")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {
    Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
        int patched = 0
        int skipped = 0

        JahiaSitesService.getInstance().getSitesNodeList(session).each { siteNode ->
            String siteKey = siteNode.getName()

            // systemsite does not expose user/group management and does not need this protection
            if (siteKey == JahiaSitesService.SYSTEM_SITE_KEY) {
                log.debug("Skipping systemsite")
                skipped++
                return // continue to next site
            }

            log.info("Processing site: {}", siteKey)
            boolean changed = false

            // Node types and ACEs mirror what site.xml defines for new sites.
            Map<String, String> folderNodeTypes = ["users": "jnt:usersFolder", "groups": "jnt:groupsFolder"]

            Map<String, String> requiredAces = [
                "g:site-administrators": "site-administrator",
                "g:users"              : "reader",
                "g:privileged"         : "privileged",
                "g:site-privileged"    : "privileged"
            ]

            folderNodeTypes.each { String folderName, String nodeType ->
                JCRNodeWrapper folder
                if (!siteNode.hasNode(folderName)) {
                    log.info("  Creating missing '{}' folder ({}) on site '{}'", folderName, nodeType, siteKey)
                    folder = siteNode.addNode(folderName, nodeType)
                    changed = true
                } else {
                    folder = siteNode.getNode(folderName)
                }

                String folderPath = folder.getPath()

                // 1. Break ACL inheritance so site-level roles (editor, translator, …) do not flow
                //    down into the users/groups subtree.
                if (!folder.getAclInheritanceBreak()) {
                    log.info("  Breaking ACL inheritance on {}", folderPath)
                    folder.setAclInheritanceBreak(true)
                    changed = true
                } else {
                    log.info("  ACL inheritance already broken on {}", folderPath)
                }

                // 2. Ensure all GRANT ACEs are present, mirroring what site.xml defines for new sites.
                requiredAces.each { String principal, String role ->
                    String aceName = "GRANT_" + principal.replace(":", "_")
                    boolean aceExists = folder.hasNode("j:acl") && folder.getNode("j:acl").hasNode(aceName)
                    if (!aceExists) {
                        log.info("  Adding GRANT '{}' ACE ({}) on {}", principal, role, folderPath)
                        folder.grantRoles(principal, Collections.singleton(role))
                        changed = true
                    } else {
                        log.info("  GRANT '{}' ACE ({}) already present on {}", principal, role, folderPath)
                    }
                }
            }

            if (changed) {
                patched++
            } else {
                skipped++
            }
        }

        if (patched > 0) {
            log.info("Saving changes for {} patched site(s)…", patched)
            session.save()
        }

        log.info("Migration complete: {} site(s) patched, {} site(s) already up-to-date or skipped",
                 patched, skipped)
        return null
    }
})
