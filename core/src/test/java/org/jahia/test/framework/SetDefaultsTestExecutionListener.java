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
