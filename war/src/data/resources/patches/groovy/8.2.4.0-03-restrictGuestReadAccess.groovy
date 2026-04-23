import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRObservationManager
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate

log.info("Starting migration: restrict guest read access to sensitive JCR nodes " +
        "(remove root GRANT, grant /users/guest + /modules + /mounts + /sites) on default + live workspaces")

// ── Helpers ───────────────────────────────────────────────────────────────────────────────────
// Returns the named ACE node under <node>/j:acl, or null if either is absent.
def findAce = { JCRNodeWrapper node, String aceName ->
    node.hasNode("j:acl") && node.getNode("j:acl").hasNode(aceName) ?
            node.getNode("j:acl").getNode(aceName) : null
}

// Removes the named ACE if present. Returns true if something changed.
def removeAce = { JCRSessionWrapper session, String path, String aceName ->
    JCRNodeWrapper node = session.getNode(path)
    JCRNodeWrapper ace = findAce(node, aceName)
    if (ace == null) {
        log.info("  [{}] {} ACE already absent from {} — skipping", session.getWorkspace().getName(), aceName, path)
        return false
    }
    log.info("  [{}] Removing legacy {} ACE from {}", session.getWorkspace().getName(), aceName, path)
    ace.remove()
    true
}

// Ensures <principal> has <role> granted at <path>. Delegates to JCRNodeWrapper.grantRoles,
// which is itself idempotent: it creates the GRANT ACE if missing and merges the role into
// j:roles if the ACE already exists without it. Returns true if something (might have) changed.
def ensureGrant = { JCRSessionWrapper session, String path, String principal, String role ->
    String ws = session.getWorkspace().getName()
    JCRNodeWrapper node = session.getNode(path)
    JCRNodeWrapper ace = findAce(node, "GRANT_" + principal.replace(':', '_'))
    List<String> roles = ace?.hasProperty("j:roles") ?
            ace.getProperty("j:roles").getValues().collect { it.getString() } : []
    if (roles.contains(role)) {
        log.info("  [{}] GRANT {} {} ACE already present on {} — skipping", ws, principal, role, path)
        return false
    }
    log.info("  [{}] Granting {} {} on {} (existing roles: {})", ws, principal, role, path, roles)
    node.grantRoles(principal, Collections.singleton(role))
    true
}

// ── Callback: one run per workspace ───────────────────────────────────────────────────────────
// 1. Root: drop the legacy blanket guest reader (the new root.xml no longer declares it;
//    nodes that still require guest access carry their own explicit GRANTs below).
// 2-5. Re-grant guest reader on the specific subtrees that must stay publicly readable:
//    /users/guest (guest must read its own node), /modules (template/resource resolution
//    during live rendering), /mounts (VFS mount points referenced from live pages), and
//    /sites (inherited by every site node, replacing the per-site grant from site.xml).
JCRCallback<Void> callback = { JCRSessionWrapper session ->
    String ws = session.getWorkspace().getName()
    log.info("Applying guest-access ACLs on workspace '{}'", ws)

    boolean changed = false
    changed |= removeAce(session, "/", "GRANT_u_guest")
    ["/users/guest", "/modules", "/mounts", "/sites"].each { path ->
        changed |= ensureGrant(session, path, "u:guest", "reader")
    }

    if (changed) {
        log.info("  [{}] Saving changes…", ws)
        session.save()
    } else {
        log.info("  [{}] Nothing to do — workspace already up-to-date", ws)
    }
    null
} as JCRCallback<Void>

// ── Execute on both workspaces with observation events disabled ───────────────────────────────
// This migration only rewrites ACL metadata; we disable JCR event listeners to prevent spurious
// side-effects (cache invalidation, publication, indexing churn, rule executions, …) while the
// patch runs. The flag is thread-local, so we restore it in a finally block.
JCRObservationManager.setAllEventListenersDisabled(true)
try {
    ["default", "live"].each { String workspace ->
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, callback)
    }
} finally {
    JCRObservationManager.setAllEventListenersDisabled(false)
}

log.info("Migration complete on default + live workspaces")
