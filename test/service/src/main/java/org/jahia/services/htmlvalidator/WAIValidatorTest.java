/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.htmlvalidator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test to test the accessibility check methods implemented by Jahia's WAIValidator. 
 * The test is done with HTML fragments located under the same package name as this class,
 * just appending the path "pass" and "fail", which should list failing and passing HTML scripts.
 * 
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
        URL scriptsUrl = WAIValidatorTest.class.getResource(shouldFail ? "fail" : "pass");
        File scriptsDirectory = new File(scriptsUrl.toURI());
        WAIValidator validator = new WAIValidator(Locale.ENGLISH);
        for (File file : scriptsDirectory.listFiles()) {
            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            String content = scanner.next();
            ValidatorResults results = validator.validate(content);
            if (shouldFail) {
                assertFalse("WAI validation for file " + file.getPath() + " should fail with errors or warnings", results
                        .getErrors().isEmpty() && results.getWarnings().isEmpty());
            } else {
                assertTrue("WAI validation for file " + file.getPath() + " should pass without errors or warnings", results
                        .getErrors().isEmpty() && results.getWarnings().isEmpty());
            }
        }

    }
}
