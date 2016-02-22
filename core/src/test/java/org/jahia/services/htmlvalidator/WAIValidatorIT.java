/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.htmlvalidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.htmlvalidator.ValidatorResults;
import org.jahia.services.htmlvalidator.WAIValidator;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * Unit test to test the accessibility check methods implemented by Jahia's WAIValidator.
 * The test is done with HTML fragments located under the same package name as this class,
 * just appending the path "pass" and "fail", which should list failing and passing HTML scripts.
 */
public class WAIValidatorIT extends AbstractJUnitTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testFailingFragments() throws Exception {
        findAndTestScripts(true);
    }

    @Test
    public void testPassingFragments() throws Exception {
        findAndTestScripts(false);
    }

    private void findAndTestScripts(boolean shouldFail) throws Exception {
        WAIValidator validator = new WAIValidator(Locale.ENGLISH);
        for (Resource re : SpringContextSingleton.getInstance().getResources("classpath*:org/jahia/test/services/htmlvalidator/"+(shouldFail ? "fail" : "pass") + "/*.html")) {
            InputStream is = re.getInputStream();
            String content = null;
            try {
                content = IOUtils.toString(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
            if (!StringUtils.isEmpty(content)) {
                ValidatorResults results = validator.validate(content);
                if (shouldFail) {
                    assertFalse("WAI validation for file " + re + " should fail with errors or warnings", results
                            .getErrors().isEmpty() && results.getWarnings().isEmpty());
                } else {
                    assertTrue("WAI validation for file " + re + " should pass without errors or warnings", results
                            .getErrors().isEmpty() && results.getWarnings().isEmpty());
                }
            }
        }
    }
}
