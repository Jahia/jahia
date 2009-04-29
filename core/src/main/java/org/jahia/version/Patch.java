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
package org.jahia.version;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 août 2007
 * Time: 18:41:08
 * To change this template use File | Settings | File Templates.
 */
public class Patch implements Comparable<Patch> {
    private File file;
    private String name;
    private int number;
    private String ext;

    public Patch(File file) {
        this.file = file;
        this.name = file.getName();
        int dot = name.lastIndexOf('.');
        ext = name.substring(dot+1);
        int us = name.lastIndexOf('_') + 1;
        try {
            String stringNumber = name.substring(us, name.indexOf('.',us));
            this.number = Integer.parseInt(stringNumber);
        } catch (NumberFormatException e) {
            this.number = 0;
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getExt() {
        return ext;
    }

    public int compareTo(Patch other) {
        int diff = getNumber() - other.getNumber();
        if (diff == 0) {
            diff = file.compareTo(other.getFile());
        }
        return diff;
    }
}
