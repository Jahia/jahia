/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
    private static final long serialVersionUID = -6081646750488342618L;

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
