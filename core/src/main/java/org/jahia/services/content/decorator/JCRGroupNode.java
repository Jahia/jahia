package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JCR group node decorator
 */
public class JCRGroupNode extends JCRNodeDecorator {

    private transient static Logger logger = LoggerFactory.getLogger(JCRUserNode.class);

    public JCRGroupNode(JCRNodeWrapper node) {
        super(node);
    }

}
