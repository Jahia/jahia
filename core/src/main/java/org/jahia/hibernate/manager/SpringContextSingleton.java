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
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class SpringContextSingleton {

    private static SpringContextSingleton ourInstance;
    
    public static SpringContextSingleton getInstance() {
        if (ourInstance == null) {
            ourInstance = new SpringContextSingleton();
        }
        return ourInstance;
    }
    private boolean initialized;

    private WebApplicationContext springContext;

    private SpringContextSingleton() {
    }

    public ApplicationContext getContext() {
        return springContext;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setContext(WebApplicationContext springContext) {
        this.springContext = springContext;
        initialized = true;
    }
}
