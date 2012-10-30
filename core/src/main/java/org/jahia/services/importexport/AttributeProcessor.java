package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;

public interface AttributeProcessor {
    public boolean process(JCRNodeWrapper node, String name, String value) throws RepositoryException;
}
