/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.htmlvalidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.htmlvalidator.ValidatorResults;
import org.jahia.services.htmlvalidator.WAIValidator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * Unit test to test the accessibility check methods implemented by Jahia's WAIValidator.
 * The test is done with HTML fragments located under the same package name as this class,
 * just appending the path "pass" and "fail", which should list failing and passing HTML scripts.
 */
public class WAIValidatorTest {
    
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
    }

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
