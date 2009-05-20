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
package org.jahia.services.scheduler;

import org.jahia.content.ContentObjectKey;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 21, 2008
 * Time: 2:16:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessAction implements Serializable {
    private ContentObjectKey key;
    private Set<String> langs;
    private String action;

    public ProcessAction() {
    }

    public ProcessAction(ContentObjectKey key, Set<String> langs, String action) {
        this.key = key;
        this.langs = langs;
        this.action = action;
    }

    public ContentObjectKey getKey() {
        return key;
    }

    public Set<String> getLangs() {
        return langs;
    }

    public String getAction() {
        return action;
    }
}
