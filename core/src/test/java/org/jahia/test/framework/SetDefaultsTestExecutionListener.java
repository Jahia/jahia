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
package org.jahia.test.framework;

import java.io.OutputStream;

import org.jahia.bin.Jahia;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class SetDefaultsTestExecutionListener extends
        AbstractTestExecutionListener {
    public static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) {}
    };

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        if (System.getProperty("jahia.config") == null) {
            System.setProperty("jahia.config", "");
        }
        if (System.getProperty("jahia.license") == null) {
            System.setProperty("jahia.license", "");
        }
        if (System.getProperty("jahiaWebAppRoot") == null) {
            System.setProperty("jahiaWebAppRoot", "./target/test-repo");
        }
        if (System.getProperty("derby.stream.error.field") == null) {
            System.setProperty("derby.stream.error.field", this.getClass().getCanonicalName() + ".DEV_NULL");
        }

        Jahia.setContextPath("");
    }
}
