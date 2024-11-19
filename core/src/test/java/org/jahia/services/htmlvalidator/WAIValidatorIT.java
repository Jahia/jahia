/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
