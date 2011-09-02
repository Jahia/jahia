package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * A JCR group node decorator
 */
public class JCRGroupNode extends JCRNodeDecorator {

    private transient static Logger logger = LoggerFactory.getLogger(JCRUserNode.class);

    public JCRGroupNode(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public String getDisplayableName() {
        try {
            if (getName().equals(JahiaGroupManagerService.GUEST_GROUPNAME)) {
                JahiaResourceBundle rb = new JahiaResourceBundle(null, getSession().getLocale(), SettingsBean.getInstance().getGuestGroupResourceModuleName());

                return rb.get(SettingsBean.getInstance().getGuestGroupResourceKey(), getName());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage());
        }
        return super.getDisplayableName();
    }

}
