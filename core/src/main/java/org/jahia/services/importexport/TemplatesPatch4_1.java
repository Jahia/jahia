/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 30 oct. 2006
 * Time: 11:25:11
 * To change this template use File | Settings | File Templates.
 */
public class TemplatesPatch4_1 implements TemplatesPatch {
    
    private static final transient Logger logger = Logger
            .getLogger(TemplatesPatch4_1.class);
    
    public void patchTemplates(File f, int buildNumber) {
        try {
            if(f.isDirectory()) {
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    File file = fs[i];
                    patchTemplates(file, buildNumber);
                }
            } else {
                FileInputStream fis = new FileInputStream(f);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesIn = 0;
                while ((bytesIn = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesIn);
                }
                fis.close();
                String tpl = new String(baos.toByteArray());

                Pattern p = Pattern.compile("ArrayList +([a-zA-Z][a-zA-Z0-9_]*) += Category\\.");
                Matcher m = p.matcher(tpl);
                tpl = m.replaceAll("List $1 = Category.");

                p = Pattern.compile("[^\\.]CategoryBean");
                tpl = p.matcher(tpl).replaceAll("org.jahia.data.beans.CategoryBean");

                p = Pattern.compile("FormDataManager.getInstance\\(\\)");
                tpl = p.matcher(tpl).replaceAll("FormDataManager");

                p = Pattern.compile("ArrayList +([a-zA-Z][a-zA-Z0-9_]*) += +([a-zA-Z][a-zA-Z0-9_]*).getChildCategories");
                tpl = p.matcher(tpl).replaceAll("List $1 = $2.getChildCategories");

                p = Pattern.compile("JahiaSearchResultHandlerImpl\\.ONE_HIT_BY_PAGE_PARAMETER_NAME");
                tpl = p.matcher(tpl).replaceAll("PageSearchResultBuilderImpl.ONE_HIT_BY_PAGE_PARAMETER_NAME");

                p = Pattern.compile("JahiaSearchResultHandlerImpl\\.ONLY_ONE_HIT_BY_PAGE");
                tpl = p.matcher(tpl).replaceAll("PageSearchResultBuilderImpl.ONLY_ONE_HIT_BY_PAGE");

                p = Pattern.compile("jahiaEvent\\.getParams\\(\\)");
                tpl = p.matcher(tpl).replaceAll("(org.jahia.params.ParamBean) jahiaEvent.getParams()");

                FileOutputStream fos = new FileOutputStream(f);
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fos));
                w.write(tpl);
                w.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

}
