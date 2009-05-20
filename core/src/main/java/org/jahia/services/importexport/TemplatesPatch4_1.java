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
