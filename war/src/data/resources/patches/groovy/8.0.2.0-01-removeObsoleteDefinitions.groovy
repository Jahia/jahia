import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry


import javax.jcr.*
import javax.jcr.query.Query
import javax.jcr.query.QueryResult;

def log = log;

def removedNodeTypes = new ArrayList();
def defaultModuleRemovals = new HashMap();
defaultModuleRemovals.put("module", "default");
defaultModuleRemovals.put("nodeTypes", Arrays.asList("jmix:sendAsNewsletter"));
removedNodeTypes.add(defaultModuleRemovals);

NodeTypeRegistry registry = NodeTypeRegistry.getInstance();

removedNodeTypes.forEach({ map ->
    try {
        String moduleName = map["module"];
        List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();
        map["nodeTypes"].each({ type ->
            try {
                ExtendedNodeType t = registry.getNodeType(type as String);
                nodeTypes.add(t);
            } catch (Exception e) {
                // Do nothing
            }
        });

        if (!nodeTypes.isEmpty()) {
            log.info("Removing nodeTypes [" + nodeTypes.join(",") + "] from module " + moduleName);
            deleteNodeTypes(nodeTypes.iterator(), true);
            JCRStoreService.getInstance().deployDefinitions(moduleName);
        }
    } catch(Exception e) {
        // Do nothing
        log.info(e.getMessage());
    }
})


private void deleteNodeTypes(final Iterator it, boolean unregister) throws IOException, RepositoryException {
    NodeTypeRegistry registry = NodeTypeRegistry.getInstance();

    while (it.hasNext()) {
        final ExtendedNodeType nodeType = (ExtendedNodeType) it.next();
        JCRCallback<Object> callback = new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
                JCRNodeWrapper root = jcrSessionWrapper.getNode("/");
                QueryResult qr = jcrSessionWrapper
                        .getProviderSession(root.getProvider())
                        .getWorkspace()
                        .getQueryManager()
                        .createQuery("SELECT * FROM ['" + nodeType.getName() + "']", Query.JCR_SQL2)
                        .execute();
                javax.jcr.NodeIterator nodes = qr.getNodes();
                int count = 0;
                while (nodes.hasNext()) {
                    javax.jcr.Node next = (javax.jcr.Node) nodes.next();
                    if (nodeType.isMixin() && !next.getPrimaryNodeType().isNodeType(nodeType.getName())) {
                        next.removeMixin(nodeType.getName());
                    } else {
                        next.remove();
                    }
                    if ((++count % 100) == 0) {
                        jcrSessionWrapper.save();
                    }
                }
                log.info("Called remove for nodeType: " + nodeType.getName() + " for workspace: " + jcrSessionWrapper.getWorkspace().getName());
                jcrSessionWrapper.save();
                return null;
            }
        };

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "default", null, callback);
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,"live", null, callback);
            if (unregister) {
                registry.unregisterNodeType(nodeType.getName());
            }
        } catch (Exception e) {
            //Do nothing
            log.info(e.getMessage());
        }
    }
}
