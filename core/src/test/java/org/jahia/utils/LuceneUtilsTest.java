/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
}
