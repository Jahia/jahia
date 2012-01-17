/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.jndi;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import javax.naming.Reference;
import javax.jcr.RepositoryException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 
 * User: toto
 * Date: Jan 19, 2009
 * Time: 4:09:38 PM
 * 
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
        return JahiaRepositoryImpl.create(config);
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
