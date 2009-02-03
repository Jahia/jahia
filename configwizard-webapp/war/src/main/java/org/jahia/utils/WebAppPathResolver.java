/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.jahia.utils;

import javax.servlet.ServletContext;

/**
 * <p>Title: Web application path resolver</p>
 * <p>Description: Resolves all the paths using the ServletContext.getRealPath
 * method.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class WebAppPathResolver implements PathResolver {

       ServletContext servletContext = null;

    public WebAppPathResolver() {
        this.servletContext = servletContext;
    }

    public String resolvePath(String relativePath) {
        return servletContext.getRealPath("/" + relativePath);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
