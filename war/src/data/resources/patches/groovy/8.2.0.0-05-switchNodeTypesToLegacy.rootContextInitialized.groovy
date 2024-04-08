import org.jahia.services.content.nodetypes.ExtendedNodeType
import org.jahia.services.content.nodetypes.NodeTypeRegistry

def keeps = ["jnt:text", "jnt:bigText", "jnt:linkList", "jnt:post", "jnt:topic",
                      "jmix:comments", "jnt:componentLink", "jnt:fileUpload", "jnt:workspaceSwitch", "jnt:manageRoles",
                      "jnt:simpleWorkflow", "jmix:internalLink", "jmix:externalLink", "jnt:imageReferenceLink",
                      "jnt:imageReference", "jnt:nodeLinkImageReference", "jnt:externalLinkImageReference", "jnt:imageI18nReference",
                      "jnt:nodeLinkI18nImageReference", "jnt:externalLinkI18nImageReference", "jmix:listSizeLimit", "jmix:hideInNavMenu",
                      "jmix:navMenuComponent", "jnt:navMenu", "jnt:navMenuText", "jnt:navMenuQuery"];

def source = "default";
def target = "legacy-default-components";

log.info("Check for nodetypes to switch from " + source + " to " + target + " (" + keeps.size() + " nodetypes to keep)");
NodeTypeRegistry nodeTypeRegistry = NodeTypeRegistry.getInstance();
nodeTypeRegistry.getAllNodeTypes(Arrays.asList(source)).forEach { nodeType ->
    if (keeps.contains(nodeType.getName())) {
        log.info("Keep nodetype: " + nodeType.getName() + " in " + source);
    } else {
        log.info("Switch nodetype: " + nodeType.getName() + " to " + target);
        def field = ExtendedNodeType.getDeclaredField("systemId")
        field.setAccessible(true)
        try {
            field.set(nodeType, target);
        } finally {
            field.setAccessible(false);
        }
        log.info("Successfully switched nodetype: " + nodeType.getName() + " to " + nodeType.getSystemId());
    }
}


