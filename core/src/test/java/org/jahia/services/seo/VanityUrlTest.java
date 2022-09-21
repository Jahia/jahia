/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.seo;

import org.apache.hc.core5.http.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for vanity URL creation (only processing of the URL string).
 *
 * @author Sergiy Shyrkov
 */
@RunWith(Parameterized.class)
public class VanityUrlTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { null, null }, { "", "" }, { "aaa", "/aaa" }, { "/aaa", "/aaa" }, { "/aaa/", "/aaa" },
                        { "/aaa////", "/aaa" }, { "//aaa", "/aaa" }, { "////aaa", "/aaa" }, { "/aaa/", "/aaa" },
                        { "/aaa//", "/aaa" }, { "//aaa/", "/aaa" }, { "//aaa//", "/aaa" }, { "////aaa////", "/aaa" } });
    }

    private String input;

    private String expectedResult;

    public VanityUrlTest(String input, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testVanityUrl() throws HttpException {
        assertEquals("Expected URL value for input '" + input + "' should be '" + expectedResult + "'", expectedResult,
                new VanityUrl(input, null, null).getUrl());
    }
}
