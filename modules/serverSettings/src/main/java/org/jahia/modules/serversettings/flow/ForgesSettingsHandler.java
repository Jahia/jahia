package org.jahia.modules.serversettings.flow;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.forge.Forge;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *Handler for forge settings
 */

public class ForgesSettingsHandler implements Serializable {

    private static final long serialVersionUID = 3483746034366875864L;
    static Logger logger = LoggerFactory.getLogger(ForgesSettingsHandler.class);

    private List<Forge> forges;

    private Forge forge;

    @Autowired
    private transient JCRTemplate template;

    public void init() {
        // fill forges
        forges = new ArrayList<Forge>();
        try {
            template.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (!session.getNode("/").hasNode("settings")) {
                        session.getNode("/").addNode("settings", "jnt:globalSettings");
                        session.save();
                    }
                    if (!session.getNode("/settings").hasNode("forgesSettings")) {
                        session.getNode("/settings").addNode("forgesSettings","jnt:forgesServerSettings");
                        session.save();
                    }
                    Node forgesRoot = session.getNode("/settings/forgesSettings");
                    if (forgesRoot != null) {
                        NodeIterator ni = forgesRoot.getNodes();
                        while (ni.hasNext()) {
                            Node n = ni.nextNode();
                            Forge f = new Forge();
                            f.setUrl(n.getProperty("j:url").getString());
                            f.setOldUrl(n.getProperty("j:url").getString());
                            f.setUser(n.getProperty("j:user").getString());
                            f.setPassword(n.getProperty("j:password").getString());
                            forges.add(f);
                        }
                    }
                    return null;
                }
            });
            forge = new Forge();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void save() {
        try {
            template.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node forgesRoot = session.getNode("/settings/forgesSettings");
                    NodeIterator jcrForges = forgesRoot.getNodes();
                    Node forgeNode = null;
                    while (jcrForges.hasNext()) {
                        Node jcrForge = jcrForges.nextNode();
                        if (StringUtils.equals(jcrForge.getProperty("j:url").getString(),forge.getOldUrl())) {
                            forgeNode = jcrForge;
                            break;
                        }
                    }
                    if (forgeNode == null) {
                        forgeNode = forgesRoot.addNode(JCRContentUtils.generateNodeName(forge.getUrl()),"jnt:forgeServerSettings");
                        forges.add(forge);
                    } else {
                        for (Forge f : forges) {
                            if (StringUtils.equals(f.getUrl(), forge.getOldUrl())) {
                                f.setUser(forge.getUser());
                                f.setUrl(forge.getUrl());
                                f.setOldUrl(forge.getUrl());
                                break;
                            }
                        }
                    }
                    forgeNode.setProperty("j:url", forge.getUrl());
                    forgeNode.setProperty("j:user",forge.getUser());
                    forgeNode.setProperty("j:password",forge.getPassword());
                    session.save();
                    return null;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        forge = new Forge();
    }

    public List<Forge> getForges() {
        return forges;
    }

    public Forge getForge() {
        return forge;
    }

    public void setForge(Forge forge) {
        this.forge = forge;
    }
}
