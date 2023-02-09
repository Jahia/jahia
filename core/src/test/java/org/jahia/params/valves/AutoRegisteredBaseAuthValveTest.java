/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.params.valves;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.impl.GenericPipeline;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of the {@link AutoRegisteredBaseAuthValve}.
 *
 * @author Sergiy Shyrkov
 */
public class AutoRegisteredBaseAuthValveTest {

    private static class MyAuthValve extends AutoRegisteredBaseAuthValve {

        MyAuthValve(String id, GenericPipeline authPipeline) {
            super();
            setId(id);
            setAuthPipeline(authPipeline);
        }

        @Override
        public void invoke(Object context, ValveContext valveContext) throws PipelineException {
            // do nothing
        }
    }

    private GenericPipeline pipeline;

    private int getPosition(String id) {
        Valve[] valves = pipeline.getValves();
        for (int i = 0; i < valves.length; i++) {
            if (StringUtils.equals(((BaseAuthValve) valves[i]).getId(), id)) {
                return i;
            }
        }

        return -1;
    }

    @Before
    public void setUp() throws Exception {
        pipeline = new GenericPipeline();
        BaseAuthValve valve = new HttpBasicAuthValveImpl();
        valve.setId("HttpBasicAuthValve");
        pipeline.addValve(valve);

        valve = new TokenAuthValveImpl();
        valve.setId("TokenAuthValve");
        pipeline.addValve(valve);

        valve = new LoginEngineAuthValveImpl();
        valve.setId("LoginEngineAuthValve");
        pipeline.addValve(valve);

        valve = new SessionAuthValveImpl();
        valve.setId("SessionAuthValve");
        pipeline.addValve(valve);

        valve = new CookieAuthValveImpl();
        valve.setId("CookieAuthValve");
        pipeline.addValve(valve);

        valve = new ContainerAuthValveImpl();
        valve.setId("ContainerAuthValve");
        pipeline.addValve(valve);
    }

    @Test
    public void testPositionAfter() {
        int count = pipeline.getValves().length;
        MyAuthValve myValve = new MyAuthValve("myAuthValve1", pipeline);
        myValve.setPositionAfter("SessionAuthValve");
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count + 1, pipeline.getValves().length);
        assertEquals("Valve is not at position 4", 4, getPosition(myValve.getId()));
        assertEquals("Valve is not after SessionAuthValve", getPosition("SessionAuthValve") + 1, getPosition(myValve.getId()));
        assertEquals("CookieAuthValve is not shifted to position 5", 5, getPosition("CookieAuthValve"));

        // add valve second time
        count = pipeline.getValves().length;
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count, pipeline.getValves().length);
        assertEquals("Valve is not at position 4", 4, getPosition(myValve.getId()));
    }

    @Test
    public void testPositionAtTheEnd() {
        int count = pipeline.getValves().length;
        MyAuthValve myValve = new MyAuthValve("myAuthValve1", pipeline);
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count + 1, pipeline.getValves().length);
        assertEquals("Valve is not appended to the end", pipeline.getValves().length - 1, getPosition(myValve.getId()));

        // add valve second time
        count = pipeline.getValves().length;
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count, pipeline.getValves().length);
        assertEquals("Valve is not at the end", count - 1, getPosition(myValve.getId()));
    }

    @Test
    public void testPositionAtTheTop() {
        int count = pipeline.getValves().length;
        MyAuthValve myValve = new MyAuthValve("myAuthValve1", pipeline);
        myValve.setPosition(0);
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count + 1, pipeline.getValves().length);
        assertEquals("Valve is not inserted at the top", 0, getPosition(myValve.getId()));

        // add valve second time
        count = pipeline.getValves().length;
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count, pipeline.getValves().length);
        assertEquals("Valve is not at the top", 0, getPosition(myValve.getId()));
    }

    @Test
    public void testPositionBefore() {
        int count = pipeline.getValves().length;
        MyAuthValve myValve = new MyAuthValve("myAuthValve1", pipeline);
        myValve.setPositionBefore("SessionAuthValve");
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count + 1, pipeline.getValves().length);
        assertEquals("Valve is not at position 3", 3, getPosition(myValve.getId()));
        assertEquals("Valve is not before SessionAuthValve", getPosition("SessionAuthValve") - 1, getPosition(myValve.getId()));
        assertEquals("CookieAuthValve is not shifted to position 5", 5, getPosition("CookieAuthValve"));

        // add valve second time
        count = pipeline.getValves().length;
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count, pipeline.getValves().length);
        assertEquals("Valve is not at position 3", 3, getPosition(myValve.getId()));
    }

    @Test
    public void testPositionFixed() {
        int count = pipeline.getValves().length;
        MyAuthValve myValve = new MyAuthValve("myAuthValve1", pipeline);
        myValve.setPosition(3);
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count + 1, pipeline.getValves().length);
        assertEquals("Valve is not at position 3", 3, getPosition(myValve.getId()));
        assertEquals("SessionAuthValve is not shifted to position 4", 4, getPosition("SessionAuthValve"));

        // add valve second time
        count = pipeline.getValves().length;
        myValve.afterPropertiesSet();
        assertEquals("Valve count is wrong", count, pipeline.getValves().length);
        assertEquals("Valve is not at position 3", 3, getPosition(myValve.getId()));
    }

}
