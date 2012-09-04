import org.jahia.services.content.*

import java.util.*

import javax.jcr.*

Map<String, String> mappings = new HashMap<String, String>();
mappings.put("/permissions/workflow-tasks/start-one-step-review", "/permissions/workflow-tasks/1-step-publication-start");
mappings.put("/permissions/workflow-tasks/one-step-review", "/permissions/workflow-tasks/1-step-publication-review");
mappings.put("/permissions/workflow-tasks/start-one-step-unpublish", "/permissions/workflow-tasks/1-step-unpublication-start");
mappings.put("/permissions/workflow-tasks/one-step-unpublish", "/permissions/workflow-tasks/1-step-unpublication-unpublish");

def log = log;
log.info("Start patch for workflow permissions")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
        boolean modified = false;
        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            log.info("\tChecking for permission " + mapping.getKey());
            try {
                JCRNodeWrapper perm = session.getNode(mapping.getKey());
                for (PropertyIterator iterator = perm.getWeakReferences("j:permissions"); iterator.hasNext();) {
                    JCRNodeWrapper role = iterator.nextProperty().getParent();
                    if (!role.isNodeType("jnt:role")) {
                        continue;
                    }
                    log.debug("\t\tFound reference in role: " + role.getPath());
                    RBACUtils.revokePermissionFromRole(mapping.getKey(), role.getPath(), session);
                    RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission(mapping.getValue(), session).getPath(), role.getPath(), session);
                    log.info("\t\tRole " + role.getPath() + " updated with the permission " + mapping.getValue());
                }
                modified = true;
                perm.remove();
                log.info("\t...deleted permission " + perm.getPath());
            } catch (PathNotFoundException e) {
                // permission not present -> skip it
            } catch (RepositoryException e) {
                log.errror("Unable to update permission " + mapping.getKey(), e);
            }
        }

        if (modified) {
            session.save();
        }

        return true;
    }
});

log.info("...done patch for workflow permissions.");