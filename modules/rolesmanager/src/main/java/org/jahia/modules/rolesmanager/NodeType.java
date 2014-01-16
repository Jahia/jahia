/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
 */

package org.jahia.modules.rolesmanager;

import java.io.Serializable;

public class NodeType implements Comparable<NodeType>, Serializable {
    private String name;
    private String displayName;
    private boolean set;

    public NodeType(String name, String displayName, boolean set) {
        this.name = name;
        this.displayName = displayName;
        this.set = set;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public boolean isSet() {
        return set;
    }

    @Override
    public int compareTo(NodeType o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}
