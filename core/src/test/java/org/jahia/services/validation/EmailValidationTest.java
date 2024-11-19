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
package org.jahia.services.validation;

import org.apache.hc.core5.http.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for e-mail validation regular expression.
 *
 * @author Sergiy Shyrkov
 */
@RunWith(Parameterized.class)
public class EmailValidationTest {

    // see 02-jahia-nodetypes.cnd
    private static final Pattern PATTERN = Pattern.compile("^$|[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}");

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "", true }, { "a@a.com", true }, { "a@a.b.c.com", true },
                { "a@a.b.c.info", true }, { "a@a.b.c.africa", true }, { "a.b.c@a.b.c.africa", true },
                { "A.b_c@a.b-c.d.aFRica", true }, { "A.b_c2017@a.b-c-2018.d.aFRica", true },

                { "a", false }, { "a@", false }, { "a@com", false }, { "a.b.c@a.b.c.a", false },
                { "a.b.c@a.b.c.", false }, { "a.b.c@a.b.c.africa24", false }, { "й%&.b.c@a.b.c.africa24", false } });
    }

    private String input;

    private boolean shouldMatch;

    public EmailValidationTest(String input, boolean shouldMatch) {
        this.input = input;
        this.shouldMatch = shouldMatch;
    }

    @Test
    public void testJavaRegexp() throws HttpException {
        if (shouldMatch) {
            assertTrue("Input " + input + " should match the defined pattern", PATTERN.matcher(input).matches());
        } else {
            assertFalse("Input " + input + " should NOT match the defined pattern", PATTERN.matcher(input).matches());
        }
    }
}
