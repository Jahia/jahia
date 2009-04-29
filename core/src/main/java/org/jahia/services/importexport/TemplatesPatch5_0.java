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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 7 f�vr. 2007
 * Time: 18:58:39
 * To change this template use File | Settings | File Templates.
 */
public class TemplatesPatch5_0 implements TemplatesPatch {
    
    private static final transient Logger logger = Logger
            .getLogger(TemplatesPatch5_0.class);
    
    public void patchTemplates(File f, int buildNumber) {
        try {
            if (buildNumber < 16244) {
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

                    Pattern p = Pattern.compile("org\\.compassframework\\.");
                    Matcher m = p.matcher(tpl);
                    tpl = m.replaceAll("org.compass.");

                    FileOutputStream fos = new FileOutputStream(f);
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fos));
                    w.write(tpl);
                    w.close();
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
