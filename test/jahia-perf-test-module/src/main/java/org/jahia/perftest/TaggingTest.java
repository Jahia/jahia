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
package org.jahia.perftest;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by toto on 08/12/14.
 */
@RunWith(Parameterized.class)
public class TaggingTest extends AbstractBenchmark {

    private static String siteKey = "taggingTest";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TaggingTest.class);

    private TaggingUtils taggingUtils = new TaggingUtils();

    private String testName;
    private int nbOfTags;
    private int nbContentToTag;
    private int tagsPerContent;

    public TaggingTest(String testName, int nbOfTags, int nbContentToTag, int tagsPerContent) {
        this.testName = testName;
        this.nbOfTags = nbOfTags;
        this.nbContentToTag = nbContentToTag;
        this.tagsPerContent = tagsPerContent;
    }

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[] {"10x10x10",10, 10, 10}, new Object[] {"10x20x10",10, 20, 10} );
    }



    @BeforeClass
    public static void setUpClass() {
        ContentGenerator cg = new ContentGenerator();
        if ((Boolean) cg.createSite(siteKey)) {
            cg.generateContent(siteKey, 1, 1, 1, 1, 1, false, false);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            JahiaSitesService.getInstance().removeSite(JahiaSitesService.getInstance().getSiteByKey(siteKey));
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testTagContent() {
        taggingUtils.tagContent(siteKey, taggingUtils.tagSubset(0,nbOfTags), nbContentToTag, tagsPerContent);
    }
}
