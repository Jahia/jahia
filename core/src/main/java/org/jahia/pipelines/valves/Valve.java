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
package org.jahia.pipelines.valves;

import org.jahia.pipelines.PipelineException;

/**
 * NOTE: This class will be deprecated once we merge with Summit
 *
 * General valve interface.  The actual valve interface(s) should be
 * extended by the implementing class.
 *
 * @author <a href="mailto:david@bluesunrise.com">David Sean Taylor</a>
 * @version $Id$
 *
 */
public interface Valve {

    public void invoke(Object context, ValveContext valveContext) throws PipelineException ;
    public void initialize();
}
