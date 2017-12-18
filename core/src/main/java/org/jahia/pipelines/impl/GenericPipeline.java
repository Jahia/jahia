/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.pipelines.impl;

import org.apache.commons.lang.ArrayUtils;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Flexible implementation of a {@link org.jahia.pipelines.Pipeline}.
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:david@bluesunrise.com">David Sean Taylor</a>
 * @version $Id$
 */
public class GenericPipeline implements Pipeline, ValveContext {

    /**
     * Name of this pipeline.
     */
    protected String name;

    private Valve[] valves = new Valve[0];
    private static final Object valvesLock = new Object();

    /**
     * The per-thread execution state for processing through this
     * pipeline.  The actual value is a java.lang.Integer object
     * containing the subscript into the <code>values</code> array, or
     * a subscript equal to <code>values.length</code> if the basic
     * Valve is currently being processed.
     */
    protected ThreadLocal<Integer> state = new ThreadLocal<Integer>();

    protected Map<String, Object> environment = new HashMap<String, Object>();

    public GenericPipeline() {

    }

    public Map<String, Object> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, Object> environment) {
        this.environment = environment;
    }

    public void initialize()
            throws PipelineException {

        // Initialize the valves
        for (Valve valve : valves) {
            //valves[i].setApplicationView(getApplicationView());
            valve.initialize();
        }
    }

    /**
     * Set the name of this pipeline.
     *
     * @param name Name of this pipeline.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of this pipeline.
     *
     * @return String Name of this pipeline.
     */
    public String getName() {
        return name;
    }

    public Valve[] getValves() {
        Valve[] results = new Valve[valves.length];
        System.arraycopy(valves, 0, results, 0, valves.length);
        return results;
    }

    public void setValves(Valve[] valves) {
        synchronized (valvesLock) {
            this.valves = valves;
        }
    }

    public void addValve(Valve valve) {
        // Add this Valve to the set associated with this Pipeline
        synchronized (valvesLock) {
            Valve[] results = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = valve;
            valves = results;
        }
    }

    /**
     * @since Jahia 6.6.0.0
     */
    public void addValve(int position, Valve valve) {
        // Add this Valve to the set associated with this Pipeline
        synchronized (valvesLock) {
            Valve[] results = new Valve[valves.length + 1];
            if (position == -1 || position >= valves.length) {
                System.arraycopy(valves, 0, results, 0, valves.length);
                results[valves.length] = valve;
            } else {
                results = (Valve[]) ArrayUtils.add(valves, position, valve);
            }
            valves = results;
        }
    }

    public void removeValve(Valve valve) {
        synchronized (valvesLock) {
            // Locate this Valve in our list
            int index = -1;
            for (int i = 0; i < valves.length; i++) {
                if (valve.equals(valves[i])) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                return;
            }

            // Remove this valve from our list
            Valve[] results = new Valve[valves.length - 1];
            int n = 0;
            for (int i = 0; i < valves.length; i++) {
                if (i == index) {
                    continue;
                }
                results[n++] = valves[i];
            }
            valves = results;
        }
    }

    public void invoke(Object context)
            throws PipelineException {
        // Initialize the per-thread state for this thread
        state.set(0);

        // Invoke the first Valve in this pipeline for this request
        invokeNext(context);
    }

    public void invokeNext(Object context)
            throws PipelineException {
        // Identify the current subscript for the current request thread
        int valvePos = state.get();

        if (valvePos < valves.length) {
            // Invoke the requested Valve for the current request
            // thread and increment its thread-local state.
            state.set(valvePos + 1);
            valves[valvePos].invoke(context, this);
        }
    }

    // BEGIN [added by Pascal Aubry for CAS authentication]

    /**
     * @see org.jahia.pipelines.Pipeline#hasValveOfClass(java.lang.Class)
     */
    public boolean hasValveOfClass(Class<Valve> c) {
        return getFirstValveOfClass(c) != null;
    }

    /**
     * @see org.jahia.pipelines.Pipeline#getFirstValveOfClass(java.lang.Class)
     */
    public Valve getFirstValveOfClass(Class<Valve> c) {
        for (Valve valve : this.valves) {
            if (c.isInstance(valve)) {
                return valve;
            }
        }
        return null;
    }
    // END [added by Pascal Aubry for CAS authentication]

    /**
     * @since Jahia 6.6.0.0
     */
    public int indexOf(Valve valve) {
        return ArrayUtils.indexOf(valves,valve);
    }
}
