/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.jndi;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.RepositoryImpl;

import javax.naming.Reference;
import javax.jcr.RepositoryException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 19, 2009
 * Time: 4:09:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBindableRepository extends BindableRepository {
    public JahiaBindableRepository(Reference reference) throws RepositoryException {
        super(reference);
    }

    @Override
    protected JackrabbitRepository createRepository() throws RepositoryException {
        RepositoryConfig config = RepositoryConfig.create(
                resolvePath(getReference().get(CONFIGFILEPATH_ADDRTYPE).getContent().toString()),
                resolvePath(getReference().get(REPHOMEDIR_ADDRTYPE).getContent().toString()));
        return RepositoryImpl.create(config);
    }

    public String resolvePath (String path) {
        Pattern p = Pattern.compile("(.*)\\$\\{(.*)\\}(.*)");
        Matcher m = p.matcher(path);
        while (m.matches()) {
            String key = m.group(2);
            String value = System.getProperty(key);
            if (value == null) {
                value = System.getenv(key);
            }
            if (value != null) {
                path = m.group(1) + value + m.group(3);
            } else {
                path = m.group(1) + m.group(3);
            }
            m.reset();
            m = p.matcher(path);
        }
        return path;
    }

}
