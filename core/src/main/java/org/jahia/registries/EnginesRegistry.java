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
    public JahiaEngine getEngine(final String engineName) {
        Object engineToGet = theRegistry.get(engineName);
        if (engineToGet == null) {
            engineToGet = theRegistry.get(Core_Engine.ENGINE_NAME);
        }
        return (JahiaEngine) engineToGet;
    }

    public JahiaEngine getEngineByBeanName(final String engineBeanName) {
        return (JahiaEngine) SpringContextSingleton.getInstance().getContext().getBean(engineBeanName);
    }

}
