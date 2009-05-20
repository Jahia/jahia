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

import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;

/**
 * Flexible implementation of a {@link org.jahia.pipelines.Pipeline}.
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:david@bluesunrise.com">David Sean Taylor</a>
 * @version $Id$
 */
public class GenericPipeline implements Pipeline, ValveContext {

    /** Name of this pipeline. */
    protected String name;

    private Valve[] valves = new Valve[0];

    /**
     * The per-thread execution state for processing through this
     * pipeline.  The actual value is a java.lang.Integer object
     * containing the subscript into the <code>values</code> array, or
     * a subscript equal to <code>values.length</code> if the basic
     * Valve is currently being processed.
     *
     */
    protected ThreadLocal state = new ThreadLocal();

    public GenericPipeline () {

    }

    public void initialize ()
        throws PipelineException {

        // Initialize the valves
        for (int i = 0; i < valves.length; i++) {
            //valves[i].setApplicationView(getApplicationView());
            valves[i].initialize();
        }
    }

    /**
     * Set the name of this pipeline.
     *
     * @param name Name of this pipeline.
     */
    public void setName (String name) {
        this.name = name;
    }

    /**
     * Get the name of this pipeline.
     *
     * @return String Name of this pipeline.
     */
    public String getName () {
        return name;
    }

    public Valve[] getValves () {
        synchronized (valves) {
            Valve[] results = new Valve[valves.length];
            System.arraycopy(valves, 0, results, 0, valves.length);
            return results;
        }
    }

    public void setValves(Valve[] valves) {
        this.valves = valves;
    }

    public void addValve (Valve valve) {
        // Add this Valve to the set associated with this Pipeline
        synchronized (valves) {
            Valve[] results = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = valve;
            valves = results;
        }
    }

    public void removeValve (Valve valve) {
        synchronized (valves) {
            // Locate this Valve in our list
            int index = -1;
            for (int i = 0; i < valves.length; i++) {
                if (valve == valves[i]) {
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

    public void invoke (Object context)
        throws PipelineException {
        // Initialize the per-thread state for this thread
        state.set(new Integer(0));

        // Invoke the first Valve in this pipeline for this request
        invokeNext(context);
    }

    public void invokeNext (Object context)
        throws PipelineException {
        // Identify the current subscript for the current request thread
        Integer current = (Integer) state.get();
        int valvePos = current.intValue();

        if (valvePos < valves.length) {
            // Invoke the requested Valve for the current request
            // thread and increment its thread-local state.
            state.set(new Integer(valvePos + 1));
            valves[valvePos].invoke(context, this);
        }

    }

    // BEGIN [added by Pascal Aubry for CAS authentication]
    /**
	 * @see org.jahia.pipelines.Pipeline#hasValveOfClass(java.lang.Class)
     */
    public boolean hasValveOfClass(Class c) {
    	return getFirstValveOfClass(c) != null;
    }

	/**
	 * @see org.jahia.pipelines.Pipeline#getFirstValveOfClass(java.lang.Class)
	 */
	public Valve getFirstValveOfClass(Class c) {
    	for (int i = 0; i < this.valves.length; i++) {
			if (c.isInstance(valves[i])) {
				return valves[i];
			}
		}
    	return null;
	}
    // END [added by Pascal Aubry for CAS authentication]



}
