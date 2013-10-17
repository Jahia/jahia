package org.jahia.services.content.interceptor;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueFactoryImpl;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 *
 */
public class TemplateModuleInterceptor extends BaseInterceptor {
    @Override
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        if (StringUtils.startsWith(property.getSession().getSitePath(), "/sites") && StringUtils.startsWith(property.getPath(),"/modules")) {
            String localPath = property.getSession().getNodeByIdentifier(storedValue.getString()).getPath();
            String[] path = StringUtils.split(localPath,"/");
            if (path.length > 2 && !StringUtils.equals(path[3],"templates")) {
                StringBuilder sitePath = new StringBuilder(property.getSession().getSitePath());
                for (int i=3; i < path.length;i++) {
                    sitePath.append("/").append(path[i]);
                }
                return JCRValueFactoryImpl.getInstance().createValue(property.getSession().getNode(sitePath.toString()));
            }
        }
        return storedValue;
    }
}
