package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;

public class TemplateNodeProcessor implements AttributeProcessor {
    public boolean process(JCRNodeWrapper node, String name, String value) throws RepositoryException {
        if (name.equals("j:templateNode")) {
            String templateName = StringUtils.substringAfterLast(value, "/");
            node.setProperty("j:templateName", templateName);
            return true;
        }
        return false;
    }
}
