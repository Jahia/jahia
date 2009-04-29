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

import org.jahia.utils.zip.ZipInputStream;

import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 févr. 2008
 * Time: 14:54:08
 * To change this template use File | Settings | File Templates.
 */
public class NoCloseZipInputStream extends ZipInputStream {
    public NoCloseZipInputStream(InputStream in) {
        super(in);
    }

    public void close() throws IOException {
    }

    public void reallyClose() throws IOException {
        super.close();
    }
}

