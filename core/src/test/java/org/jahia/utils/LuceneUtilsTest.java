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
package org.jahia.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christophe Laprun
 */
public class LuceneUtilsTest {

    @Test
    public void testExtractLanguage() {
        String field = LuceneUtils.getFullTextFieldName("foo", "en");
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFrom(field));

        // unfortunately, we cannot distinguish between site name and language name at the moment so using a valid
        // language code as site name would result in a positive language match
        field = LuceneUtils.getFullTextFieldName("ar", null);
        Assert.assertEquals("ar", LuceneUtils.extractLanguageOrNullFrom(field));

        // using something other than a valid language code as site name should result in a null language
        field = LuceneUtils.getFullTextFieldName("foo", null);
        Assert.assertNull(LuceneUtils.extractLanguageOrNullFrom(field));
    }

    @Test
    public void testExtractLanguageFromStatement() {
        /**
         * Extracts the language code that might be available in the form of a <code>jcr:language</code> constraint from the given query statement.
         * More specifically, the following cases should be properly handled:
         *
         * <ul>
         * <li><code>jcr:language = 'en'</code>  (in that case analyzer should be English)</li>
         * <li><code>jcr:language = "en"</code>  (in that case analyzer should be English)</li>
         * <li><code>jcr:language is null</code> (in that case analyzer cannot be determined by query language - take the default)</li>
         * <li><code>jcr:language is null and jcr:language = 'en'</code> (in that case the query language specific analyzer should be English, but it is set only on the second
         * jcr:language constraint)</li>
         * <li><code>jcr:language &lt;&gt; 'en'</code> (in that case analyzer cannot be determined by query language - take the default)</li>
         * <li><code>jcr:language = 'fr' or jcr:language='en'</code> (in that case analyzer can also not be determined by query language - take the default)</li>
         * </ul>
         *
         * @param statement the query statement from which to extract a potential language code
         * @return the language code associated with the jcr:language constraint if it exists in the specified query statement or <code>null</code> otherwise.
         */
        String statement = "foo bar jcr:language baz blah \t \n jcr:language  =      '   en \t'       asdf asdn   ";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "foo bar jcr:language baz blah \t \n jcr:language  =      \"   en \t\"       asdf asdn   ";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "jcr:language='en'";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "jcr:language=\"en\"";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "   foo bar    jcr:language is null baz asdf";
        Assert.assertNull(LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "foo bar jcr:language is null and jcr:language = 'en'   asdaf   ";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "   foo bar    jcr:language <   > 'en'   baz asdf";
        Assert.assertNull(LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "   foo bar    jcr:language = 'fr' or jcr:language='en'   baz asdf";
        Assert.assertNull(LuceneUtils.extractLanguageOrNullFromStatement(statement));

        statement = "    faijdsfsd asdfasd ([jcr:language] = \"en\" or [jcr:language] is null) and [someOtherProperty] = \"test\" foo bar    ";
        Assert.assertEquals("en", LuceneUtils.extractLanguageOrNullFromStatement(statement));
    }
}
