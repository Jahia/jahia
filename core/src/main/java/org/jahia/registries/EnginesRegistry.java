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
//
//  EV      19.11.2000
//  AK      14.12.2000   temporary solution to catch getEngine() fault.
//  AK      14.12.2000   add the core engine.
//  AK      19.12.2000   add the managefield engine.
//	DJ				   07.02.2001	added logout engine.

package org.jahia.registries;

import org.jahia.engines.JahiaEngine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.hibernate.manager.SpringContextSingleton;

import java.util.*;

public class EnginesRegistry {

    private static EnginesRegistry instance = null;
    private final Map theRegistry;
    private List engineInstances = new ArrayList();

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(EnginesRegistry.class);


    /**
     * constructor EV    19.11.2000
     */
    public EnginesRegistry() {
        theRegistry = new HashMap();
    }

    public List getEngineInstances() {
        return engineInstances;
    }

    public void setEngineInstances(List engineInstances) {
        this.engineInstances = engineInstances;
        Iterator engineInstanceIterator = engineInstances.iterator();
        while (engineInstanceIterator.hasNext()) {
            JahiaEngine currentJahiaEngine = (JahiaEngine) engineInstanceIterator.next();
            theRegistry.put(currentJahiaEngine.getName(), currentJahiaEngine);
        }
    }

    /**
     * getInstance EV    19.11.2000
     */
    public static synchronized EnginesRegistry getInstance() {
        if (instance == null) {
          instance = (EnginesRegistry) SpringContextSingleton.getInstance().getContext().getBean("engineRegistry");
        }
        return instance;
    }


    /**
     * init EV    19.11.2000 FIXME     : load registry from database ? AK      14.12.2000 : add
     * the core engine... AK      19.12.2000 : add the managefield engine...
     * <p/>
     * called from init() method in Jahia servlet
     */
    public void init() {
    }


    /**
     * getEngine EV    19.11.2000 AK      14.12.2000 : temporary solution to catch getEngine()
     * fault. if the engine doesn't exist, core is selected...
     */
    public Object getEngine(final String engineName) {
        Object engineToGet = theRegistry.get(engineName);
        if (engineToGet == null) {
            engineToGet = theRegistry.get(Core_Engine.ENGINE_NAME);
        }
        return engineToGet;
    }

    public JahiaEngine getEngineByBeanName(final String engineBeanName) {
        return (JahiaEngine) SpringContextSingleton.getInstance().getContext().getBean(engineBeanName);
    }

}
