/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test for e-mail validation regular expression.
 * 
 * @author Sergiy Shyrkov
 */
@RunWith(Parameterized.class)
public class EmailValidationTest {

    // see 02-jahia-nodetypes.cnd
    private static final Pattern PATTERN = Pattern.compile("^$|^[A-Za-z0-9]+[A-Za-z0-9._'%+-]*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}");

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "", true },                               // 1
                { "a@a.com", true },                        // 2
                { "a@a.b.c.com", true },                    // 3
                { "a@a.b.c.info", true },                   // 4
                { "a@a.b.c.africa", true },                 // 5
                { "a.b.c@a.b.c.africa", true },             // 6
                { "A.b_c@a.b-c.d.aFRica", true },           // 7
                { "A.b_c2017@a.b-c-2017.d.aFRica", true },  // 8
                { "mike.o'brian@gmail.com", true },         // 9

                { "a", false },                             // 10
                { "a@", false },                            // 11
                { "a@com", false },                         // 12
                { "a.b.c@a.b.c.a", false },                 // 13
                { "a.b.c@a.b.c.", false },                  // 14
                { "a.b.c@a.b.c.africa24", false },          // 15
                { "й%&.b.c@a.b.c.africa24", false },        // 16
                { ".aaaa@aaa.com", false },                 // 17
        });
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
