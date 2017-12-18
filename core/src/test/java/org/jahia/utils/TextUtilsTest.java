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
package org.jahia.utils;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;

/**
 *
 * @author Christophe Laprun
 */
public class TextUtilsTest {
    private static final String REPLACEMENT = "REPLACEMENT";
    private static final String PREFIX = "PREFIX";
    private static final String SUFFIX = "SUF";
    private static final TestStringReplacementGenerator DEF_GEN = new TestStringReplacementGenerator(REPLACEMENT);
    private static final String CACHE_ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String CACHE_ESI_TAG_END = "\"></jahia_esi:include>";
    public static final TextUtils.ReplacementGenerator GENERATOR = new TextUtils.ReplacementGenerator() {
        @Override
        public void appendReplacementForMatch(int matchStart, int matchEnd, char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix) {
            // expects match to start with: src="<what we want to extract>"
            int firstQuoteIndex = matchStart;
            while (initialStringAsCharArray[firstQuoteIndex++] != '"') ;

            int secondQuoteIndex = firstQuoteIndex + 1;
            while (initialStringAsCharArray[secondQuoteIndex++] != '"') ;

            builder.append(CACHE_ESI_TAG_START)
                    .append(initialStringAsCharArray, firstQuoteIndex, secondQuoteIndex - firstQuoteIndex - 1)
                    .append(CACHE_ESI_TAG_END);
        }
    };


    @Test
    public void testNestedBounds() {
        final String prefix = "<tag";
        final String suffix = "/tag>";
        final TextUtils.ReplacementGenerator generator = new TextUtils.ReplacementGenerator() {
            @Override
            public void appendReplacementForMatch(int matchStart, int matchEnd, char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix) {
                int firstQuoteIndex = matchStart;
                while (initialStringAsCharArray[firstQuoteIndex++] != '\'') ;

                int secondQuoteIndex = firstQuoteIndex + 1;
                while (initialStringAsCharArray[secondQuoteIndex++] != '\'') ;

                builder.append("<new src='").append(initialStringAsCharArray, firstQuoteIndex, secondQuoteIndex - firstQuoteIndex - 1).append("'></new>");
            }
        };

        String initial = "aaa<tag src='1'>bbb<tag src='2'>ccc<tag src='3'>dddd<tag src='4'>eee</tag>ddd</tag>cccc</tag>bbb</tag>aaa<tag src='5'>foo</tag>";
        String expected = "aaa<new src='1'></new>aaa<new src='5'></new>";
        assertEquals(expected, TextUtils.replaceBoundedString(initial, prefix, suffix, generator));


        initial = "aaa<tag src='1'>bbb<tag src='2'>ccc</tag>  <tag src='3'>dddd<tag src='4'>eee</tag>ddd</tag> bbb</tag> aaa <tag src='5'>foo</tag>    ffff";
        expected = "aaa<new src='1'></new> aaa <new src='5'></new>    ffff";
        assertEquals(expected, TextUtils.replaceBoundedString(initial, prefix, suffix, generator));

        initial = "foo <tag src='1'> aaaa\n\n           <tag src='2'>\n bbbbbb </tag>\n\t    cccccccc    <tag src='3'>\n" +
                " dddd     </tag>\n eee <tag src='4'>\n    ffff </tag>\n g <tag src='5'>\n gggg </tag>\n hhhh </tag> bar";
        expected = "foo <new src='1'></new> bar";
        assertEquals(expected, TextUtils.replaceBoundedString(initial, prefix, suffix, generator));
    }

    @Test
    public void testReplaceAllInstancesOfBoundedString() {
        assertEquals("", TextUtils.replaceBoundedString("", PREFIX, SUFFIX, REPLACEMENT));
        assertEquals("", TextUtils.replaceBoundedString("", PREFIX, "", REPLACEMENT));
        assertEquals("", TextUtils.replaceBoundedString("", "", "", REPLACEMENT));

        DEF_GEN.setExpectedMatches("");
        assertEquals(REPLACEMENT, TextUtils.replaceBoundedString("PREFIXSUF", PREFIX, SUFFIX, DEF_GEN));

        DEF_GEN.setExpectedMatches("bbbbb");
        assertEquals("aaaaREPLACEMENTccccc", TextUtils.replaceBoundedString("aaaaPREFIXbbbbbSUFccccc", PREFIX, SUFFIX, DEF_GEN));

        DEF_GEN.setExpectedMatches((String) null);
        assertEquals("aaaPREFIXbbbbSUFF", TextUtils.replaceBoundedString("aaaPREFIXbbbbSUFF", PREFIX, "SUFFI", DEF_GEN));
        assertEquals("aRcccReeeR", TextUtils.replaceBoundedString("aPbbScccPdSeeePS", "P", "S", "R"));
    }

    @Test
    public void testReplaceBoundedString() {
        final String empty = "";
        buildTestWithDefaults(REPLACEMENT, PREFIX + SUFFIX, empty);
        buildTest(empty, PREFIX + SUFFIX, empty, PREFIX, SUFFIX, empty);
        buildTest("RaRcccReeeR", "PSaPScccPdSeeePS", "R", "P", "S", empty, empty, "d", empty);
    }

    // To quickly check memory usage in IDEA's unit test runner
    @Test
    public void testMemory() {
        timeStrategy(100, ReplacementStrategy.TEXT_UTILS);
    }

    @Test
    public void performanceCheck() {
        final int numberOfRuns = 100;

        long jerichoTime = timeStrategy(numberOfRuns, ReplacementStrategy.JERICHO);
        long textUtilsTime = timeStrategy(numberOfRuns, ReplacementStrategy.TEXT_UTILS);

        float ratio = ((float) jerichoTime) / textUtilsTime;

        System.out.println("TextUtils took " + textUtilsTime + " ms");
        System.out.println("Jericho took " + jerichoTime + " ms");
        System.out.println("Ratio jericho time / TextUtils time: " + ratio);
    }

    private long timeStrategy(int numberOfRuns, ReplacementStrategy strategy) {
        int runNb = 0;
        long begin = System.currentTimeMillis();
        while (runNb++ < numberOfRuns) {
            checkCacheEntryReplacement(strategy);
        }
        return System.currentTimeMillis() - begin;
    }

    @Test
    public void checkCacheEntryReplacement() {
        checkCacheEntryReplacement(ReplacementStrategy.TEXT_UTILS);
    }

    public void checkCacheEntryReplacement(ReplacementStrategy strategy) {
        String initial = "<!-- jahia:temp value=\"URLParserStart80348f39\" --><ul class=\"nav\"><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@608c6eab-e7b1-4802-be88-429838ad53fc@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart2b56ff98\" --><a href=\"/jahia/sites/ACMESPACE/home/activities.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">Activities <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEnd2b56ff98\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities/space-exploration@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fspace-exploration@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@52aa2c87-e2bc-4165-a808-857ec19c91a7@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart10a1bffd\" --><a href=\"/jahia/sites/ACMESPACE/home/activities/space-exploration.html\">Space Exploration</a><!-- jahia:temp value=\"URLParserEnd10a1bffd\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities/page@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fpage@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@96969f18-887a-4bb8-a9fa-693e7970f93a@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart67864df9\" --><a href=\"/jahia/sites/ACMESPACE/home/activities/page.html\">Satellites</a><!-- jahia:temp value=\"URLParserEnd67864df9\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/events@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fevents@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@4fa89c58-95fe-45f4-8dec-ceb0aaba84e3@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartd0907556\" --><a href=\"/jahia/sites/ACMESPACE/home/events.html\">Events</a><!-- jahia:temp value=\"URLParserEndd0907556\" -->\n" +
                "<!-- /cache:include --></li><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@b1f52ec7-6527-4331-a02b-43e2bf412d50@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart076e7b64\" --><a href=\"/jahia/sites/ACMESPACE/home/news.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">News <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEnd076e7b64\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news/corporate-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fcorporate-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@efc937b2-2b10-45df-9e99-ba678da54655@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart6dc04b20\" --><a href=\"/jahia/sites/ACMESPACE/home/news/corporate-news.html\">Corporate News</a><!-- jahia:temp value=\"URLParserEnd6dc04b20\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news/all-acme-space-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fall-acme-space-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@eafcf714-0a66-4be5-8f8c-8c1d48947b14@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStarta7f739a9\" --><a href=\"/jahia/sites/ACMESPACE/home/news/all-acme-space-news.html\">All ACME SPACE News</a><!-- jahia:temp value=\"URLParserEnda7f739a9\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@1c5c3953-9578-436a-af4c-965b51588da3@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartee7c894f\" --><a href=\"/jahia/sites/ACMESPACE/home/community.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">Community <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEndee7c894f\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/space-blogs@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fspace-blogs@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@3ad754a4-09a6-4617-ba48-01b195400048@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartd0b3c9e2\" --><a href=\"/jahia/sites/ACMESPACE/home/community/space-blogs.html\">Space Blogs</a><!-- jahia:temp value=\"URLParserEndd0b3c9e2\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/knowledge-base@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fknowledge-base@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@b1ac257d-84a8-4c84-a34e-3ee1ee6ee48f@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart7090746a\" --><a href=\"/jahia/sites/ACMESPACE/home/community/knowledge-base.html\">Knowledge base</a><!-- jahia:temp value=\"URLParserEnd7090746a\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/publication@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fpublication@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@47918292-b46d-48cf-a86d-b3bfb36349c1@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartab928648\" --><a href=\"/jahia/sites/ACMESPACE/home/community/publication.html\">Publication</a><!-- jahia:temp value=\"URLParserEndab928648\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/forums@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fforums@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@f1a30df9-793f-4e6d-b187-b65a2257facf@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartf457343c\" --><a href=\"/jahia/sites/ACMESPACE/home/community/forums.html\">Forums</a><!-- jahia:temp value=\"URLParserEndf457343c\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/about-us@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fabout-us@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@721db48b-306c-46ab-ab18-dba93c0e1e27@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart24d7a909\" --><a href=\"/jahia/sites/ACMESPACE/home/about-us.html\">About Us</a><!-- jahia:temp value=\"URLParserEnd24d7a909\" -->\n" +
                "<!-- /cache:include --></li></ul><!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrap-navigation-menu_mr_@@addResources@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrap-navigation-menu,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@f8280472-4c8f-4396-be4f-e2df81f102de@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartbff3991d\" --><jahia:resource type=\"css\" path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap.css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-dropdown.js\" insert=\"false\" resource=\"bootstrap-dropdown.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap-components%2Fjavascript%2Ftwitter-bootstrap-hover-dropdown.js\" insert=\"false\" resource=\"twitter-bootstrap-hover-dropdown.js\" title=\"\" key=\"\" /><!-- jahia:temp value=\"URLParserEndbff3991d\" -->\n" +
                "<!-- /cache:include --><!-- jahia:temp value=\"URLParserEnd80348f39\" -->";
        String replaced = "<!-- jahia:temp value=\"URLParserStart80348f39\" --><ul class=\"nav\"><li class=\"dropdown\"><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/activities@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@608c6eab-e7b1-4802-be88-429838ad53fc@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><ul class=\"dropdown-menu\"><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/activities/space-exploration@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fspace-exploration@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@52aa2c87-e2bc-4165-a808-857ec19c91a7@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/activities/page@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fpage@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@96969f18-887a-4bb8-a9fa-693e7970f93a@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li></ul></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/events@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fevents@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@4fa89c58-95fe-45f4-8dec-ceb0aaba84e3@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li class=\"dropdown\"><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/news@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@b1f52ec7-6527-4331-a02b-43e2bf412d50@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><ul class=\"dropdown-menu\"><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/news/corporate-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fcorporate-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@efc937b2-2b10-45df-9e99-ba678da54655@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/news/all-acme-space-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fall-acme-space-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@eafcf714-0a66-4be5-8f8c-8c1d48947b14@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li></ul></li><li class=\"dropdown\"><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/community@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@1c5c3953-9578-436a-af4c-965b51588da3@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><ul class=\"dropdown-menu\"><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/community/space-blogs@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fspace-blogs@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@3ad754a4-09a6-4617-ba48-01b195400048@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/community/knowledge-base@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fknowledge-base@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@b1ac257d-84a8-4c84-a34e-3ee1ee6ee48f@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/community/publication@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fpublication@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@47918292-b46d-48cf-a86d-b3bfb36349c1@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/community/forums@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fforums@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@f1a30df9-793f-4e6d-b187-b65a2257facf@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li></ul></li><li><jahia_esi:include src=\"en@@/sites/ACMESPACE/home/about-us@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fabout-us@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@721db48b-306c-46ab-ab18-dba93c0e1e27@@true@@ACMESPACE:null@@{}\"></jahia_esi:include></li></ul><jahia_esi:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrap-navigation-menu_mr_@@addResources@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrap-navigation-menu,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/46c7e2cc-085c-4c1c-a459-e2885447e954/base|null/d1aa0bf3-aa7b-4a79-98af-206b188ba247/home@@f8280472-4c8f-4396-be4f-e2df81f102de@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><!-- jahia:temp value=\"URLParserEnd80348f39\" -->";

        assertEquals(replaced, strategy.replace(initial));

        initial = "<!-- jahia:temp value=\"URLParserStart882f6c25\" --><jahia:resource type=\"css\" path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap" +
                ".css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-collapse.js\" insert=\"false\" resource=\"bootstrap-collapse.js\" title=\"\" key=\"\" />\n" +
                "\n" +
                "<div class=\"navbar navbar-fixed-top navbar-inverse\">\n" +
                "    <div class=\"navbar-inner\">\n" +
                "    \n" +
                "        <div class=\"container\">\n" +
                "            <a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "            </a>\n" +
                "    \n" +
                "\n" +
                "        <a class=\"brand\" href=\"/jahia/sites/ACMESPACE/home.html\"></a>\n" +
                "\n" +
                "        \n" +
                "            <div class=\"nav-collapse collapse\">\n" +
                "        <!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrap-navigation-menu_mr_@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrap-navigation-menu,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@a8f9033c-2a08-4ff6-9107-d5a4dbbc841e@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart42b0d3db\" --><ul class=\"nav\"><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@3f8515f0-7108-471f-a327-33bae5083e65@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart4f4e6189\" --><a href=\"/jahia/sites/ACMESPACE/home/activities.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">Activities <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEnd4f4e6189\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities/space-exploration@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fspace-exploration@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@c56c9ddc-195a-4c16-966d-414b6de1c578@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStarta3a085b3\" --><a href=\"/jahia/sites/ACMESPACE/home/activities/space-exploration.html\">Space Exploration</a><!-- jahia:temp value=\"URLParserEnda3a085b3\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/activities/page@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Factivities%2Fpage@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@16d00259-05fc-4cc3-8cbc-dad7871113d3@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartdd29da1e\" --><a href=\"/jahia/sites/ACMESPACE/home/activities/page.html\">Satellites</a><!-- jahia:temp value=\"URLParserEnddd29da1e\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/events@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fevents@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@20b90e08-17e6-4d18-a5ab-fdeea771f0c2@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart15d48b8e\" --><a href=\"/jahia/sites/ACMESPACE/home/events.html\">Events</a><!-- jahia:temp value=\"URLParserEnd15d48b8e\" -->\n" +
                "<!-- /cache:include --></li><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@83f3965c-b567-486a-8535-6fdb9395db0c@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart4114685c\" --><a href=\"/jahia/sites/ACMESPACE/home/news.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">News <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEnd4114685c\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news/corporate-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fcorporate-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@d78a85a6-ea2a-4896-a761-a47c0f7c14e0@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartda632932\" --><a href=\"/jahia/sites/ACMESPACE/home/news/corporate-news.html\">Corporate News</a><!-- jahia:temp value=\"URLParserEndda632932\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/news/all-acme-space-news@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fnews%2Fall-acme-space-news@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@a545ea99-14ec-4561-9edf-95d18cc7565f@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartf134ece0\" --><a href=\"/jahia/sites/ACMESPACE/home/news/all-acme-space-news.html\">All ACME SPACE News</a><!-- jahia:temp value=\"URLParserEndf134ece0\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li class=\"dropdown\"><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community@@menuDropdown@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@a6805569-f3a3-4373-8858-6aca98f10959@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart12c4f211\" --><a href=\"/jahia/sites/ACMESPACE/home/community.html\" data-hover=\"dropdown\" data-delay=\"500\" class=\"dropdown-toggle\">Community <b class=\"caret\"></b></a><!-- jahia:temp value=\"URLParserEnd12c4f211\" -->\n" +
                "<!-- /cache:include --><ul class=\"dropdown-menu\"><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/space-blogs@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fspace-blogs@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@4c2a2b3a-4e93-4d14-a30c-d27131d17e63@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartea36316e\" --><a href=\"/jahia/sites/ACMESPACE/home/community/space-blogs.html\">Space Blogs</a><!-- jahia:temp value=\"URLParserEndea36316e\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/knowledge-base@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fknowledge-base@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@57fc946b-935f-4994-8267-9c8c22c7b3ec@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart07425075\" --><a href=\"/jahia/sites/ACMESPACE/home/community/knowledge-base.html\">Knowledge base</a><!-- jahia:temp value=\"URLParserEnd07425075\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/publication@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fpublication@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@fc73faa4-3d2f-42a2-9474-39d67eab93b4@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart69b5ad41\" --><a href=\"/jahia/sites/ACMESPACE/home/community/publication.html\">Publication</a><!-- jahia:temp value=\"URLParserEnd69b5ad41\" -->\n" +
                "<!-- /cache:include --></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/community/forums@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fcommunity%2Fforums@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@28c0fbf7-2902-499f-ae4a-ed4af8b2aeaa@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart9fad2a31\" --><a href=\"/jahia/sites/ACMESPACE/home/community/forums.html\">Forums</a><!-- jahia:temp value=\"URLParserEnd9fad2a31\" -->\n" +
                "<!-- /cache:include --></li></ul></li><li><!-- cache:include src=\"en@@/sites/ACMESPACE/home/about-us@@menuElement@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fabout-us@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@f78fc680-7989-4a37-9d65-9ab48aa94b8e@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart92ecddd3\" --><a href=\"/jahia/sites/ACMESPACE/home/about-us.html\">About Us</a><!-- jahia:temp value=\"URLParserEnd92ecddd3\" -->\n" +
                "<!-- /cache:include --></li></ul><!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrap-navigation-menu_mr_@@addResources@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrap-navigation-menu,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@a8f9033c-2a08-4ff6-9107-d5a4dbbc841e@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart524fdcb7\" --><jahia:resource type=\"css\" path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap.css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-dropdown.js\" insert=\"false\" resource=\"bootstrap-dropdown.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap-components%2Fjavascript%2Ftwitter-bootstrap-hover-dropdown.js\" insert=\"false\" resource=\"twitter-bootstrap-hover-dropdown.js\" title=\"\" key=\"\" /><!-- jahia:temp value=\"URLParserEnd524fdcb7\" -->\n" +
                "<!-- /cache:include --><!-- jahia:temp value=\"URLParserEnd42b0d3db\" -->\n" +
                "<!-- /cache:include --><!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenusimplesearchform@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrapmenusimplesearchform@@module@@false@@@@_qs[src_terms*, ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@5c579af7-b636-4e6d-823d-9aaa8f5557c9@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartd4fbac64\" --><jahia:resource type=\"css\" path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap.css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "\n" +
                "<form method=\"post\" name=\"searchForm\" action=\"/jahia/sites/ACMESPACE/search-results.html\" class=\"navbar-search pull-left\" >\n" +
                "    <input type=\"hidden\" name=\"jcrMethodToCall\" value=\"get\" />\n" +
                "    <input type=\"hidden\" name=\"src_originSiteKey\" value=\"ACMESPACE\"/>\n" +
                "    \n" +
                "<input onblur=\"if(this.value=='')this.value='Start Searching ...';\" onfocus=\"if(this.value=='Start Searching ...')this.value='';\" name=\"src_terms[0].term\" id=\"searchTerm\" type=\"text\" class=\"search-query\"  value=\"Start Searching ...\"/>\n" +
                "<input type=\"hidden\" name=\"src_terms[0].applyFilter\" value=\"true\"/>\n" +
                "\n" +
                "    <input type=\"hidden\" name=\"src_terms[0].match\" value=\"all_words\"/>\n" +
                "\n" +
                "    <input type=\"hidden\" name=\"src_terms[0].fields.siteContent\" value=\"true\"/>\n" +
                "\n" +
                "    <input type=\"hidden\" name=\"src_terms[0].fields.tags\" value=\"true\"/>\n" +
                "<input type=\"hidden\" name=\"src_sites.values\" value=\"ACMESPACE\"/>\n" +
                "    <input type=\"hidden\" name=\"src_sitesForReferences.values\" value=\"systemsite\"/>\n" +
                "<input type=\"hidden\" name=\"src_languages.values\" value=\"en\"/>\n" +
                "</form><!-- jahia:temp value=\"URLParserEndd4fbac64\" -->\n" +
                "<!-- /cache:include --><!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenuloginform_mr_@@menu@@html@@_perUser_@@module@@false@@@@_qs[loginError, ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@d4819ff5-0219-4b16-829f-cbdbc5c1bf74@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartde237ccb\" --><jahia:resource type=\"css\" path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap.css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-alert.js\" insert=\"false\" resource=\"bootstrap-alert.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-modal.js\" insert=\"false\" resource=\"bootstrap-modal.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-transition.js\" insert=\"false\" resource=\"bootstrap-transition.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-collapse.js\" insert=\"false\" resource=\"bootstrap-collapse.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"css\" path=\"%2Fjahia%2Fmodules%2Fbootstrap-acme-space-templates%2Fcss%2FbootstrapComponents.css\" insert=\"false\" resource=\"bootstrapComponents.css\" title=\"\" key=\"\" />\n" +
                "\n" +
                "    <div class=\"user-box dropdown\">\n" +
                "\n" +
                "        \n" +
                "\n" +
                "        <a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">\n" +
                "            \n" +
                "                <img class='user-photo' src=\"/jahia/modules/bootstrap-components/images/user.png\" alt=\"\" width=\"60\"\n" +
                "                     height=\"32\"/>\n" +
                "            root&nbsp; <span class=\"caret\"></span>\n" +
                "        </a>\n" +
                "        <ul class=\"dropdown-menu\">\n" +
                "            \n" +
                "                    <li>\n" +
                "                        <a href=\"/jahia/cms/render/default/en/sites/ACMESPACE/home.html\">\n" +
                "                            <i class=\"icon-eye-open\"></i>\n" +
                "                            Preview\n" +
                "                        </a>\n" +
                "                    </li>\n" +
                "                \n" +
                "                    <li>\n" +
                "                        <a href=\"/jahia/cms/edit/default/en/sites/ACMESPACE/home.html\">\n" +
                "                            <i class=\"icon-edit\"></i>\n" +
                "                            Edit\n" +
                "                        </a>\n" +
                "                    </li>\n" +
                "                \n" +
                "            <li class=\"divider\"></li>\n" +
                "            <li>\n" +
                "                <a href=\"/jahia/start\">\n" +
                "                    <i class=\"icon-user\"></i>\n" +
                "                    Profile\n" +
                "                </a>\n" +
                "            </li>\n" +
                "\n" +
                "            <li class=\"divider\"></li>\n" +
                "\n" +
                "            <li>\n" +
                "                <a href=\"/jahia/cms/logout\">\n" +
                "                    <i class=\"icon-off\"></i>\n" +
                "                    Logout\n" +
                "                </a>\n" +
                "            </li>\n" +
                "        </ul>\n" +
                "    </div><!-- jahia:temp value=\"URLParserEndde237ccb\" -->\n" +
                "<!-- /cache:include --><!-- cache:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenulanguageswitcher_mr_@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrapmenulanguageswitcher,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@7e71f45d-1700-46e5-bc51-c22b8e21c47a@@true@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart8acc4b64\" --><jahia:resource type=\"css\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fcss%2FlanguageSwitchingLinks.css\" insert=\"false\" resource=\"languageSwitchingLinks.css\" title=\"\" key=\"\" />\n" +
                "\n" +
                "    <ul class=\"nav pull-right\">\n" +
                "        <li class=\"dropdown\">\n" +
                "            \n" +
                "                    <a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">EN<span\n" +
                "                            class=\"caret\"></span></a>\n" +
                "                \n" +
                "            <ul class=\"dropdown-menu\">\n" +
                "                \n" +
                "                        <li><a title=\"Switch to\"\n" +
                "                               href=\"/jahia/fr/sites/ACMESPACE/home.html##requestParameters##\">FR</a>\n" +
                "                        </li>\n" +
                "                    \n" +
                "            </ul>\n" +
                "        </li>\n" +
                "    </ul><!-- jahia:temp value=\"URLParserEnd8acc4b64\" -->\n" +
                "<!-- /cache:include -->\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    \n" +
                "    </div>\n" +
                "</div><!-- jahia:temp value=\"URLParserEnd882f6c25\" -->";
        replaced = "<!-- jahia:temp value=\"URLParserStart882f6c25\" --><jahia:resource type=\"css\" " +
                "path=\"/jahia/files/live/sites/ACMESPACE/files/bootstrap/css/bootstrap.css\" insert=\"true\" resource=\"bootstrap.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap%2Fjavascript%2Fbootstrap-collapse.js\" insert=\"false\" resource=\"bootstrap-collapse.js\" title=\"\" key=\"\" />\n" +
                "\n" +
                "<div class=\"navbar navbar-fixed-top navbar-inverse\">\n" +
                "    <div class=\"navbar-inner\">\n" +
                "    \n" +
                "        <div class=\"container\">\n" +
                "            <a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "            </a>\n" +
                "    \n" +
                "\n" +
                "        <a class=\"brand\" href=\"/jahia/sites/ACMESPACE/home.html\"></a>\n" +
                "\n" +
                "        \n" +
                "            <div class=\"nav-collapse collapse\">\n" +
                "        <jahia_esi:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrap-navigation-menu_mr_@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrap-navigation-menu,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@a8f9033c-2a08-4ff6-9107-d5a4dbbc841e@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><jahia_esi:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenusimplesearchform@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrapmenusimplesearchform@@module@@false@@@@_qs[src_terms*, ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@5c579af7-b636-4e6d-823d-9aaa8f5557c9@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><jahia_esi:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenuloginform_mr_@@menu@@html@@_perUser_@@module@@false@@@@_qs[loginError, ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@d4819ff5-0219-4b16-829f-cbdbc5c1bf74@@true@@ACMESPACE:null@@{}\"></jahia_esi:include><jahia_esi:include src=\"en@@/modules/bootstrap-acme-space-templates/3.0.0-SNAPSHOT/templates/base/header/home/bootstrapmenulanguageswitcher_mr_@@default@@html@@%2Fmodules%2Fbootstrap-acme-space-templates%2F3.0.0-SNAPSHOT%2Ftemplates%2Fbase%2Fheader%2Fhome%2Fbootstrapmenulanguageswitcher,_mraclmr_@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@bootstrap/9846c0ae-c19a-4d28-ad0d-3a0c34cef78f/base|null/a76bfefb-aad7-4ced-a62c-dbdb2b8acc51/home@@7e71f45d-1700-46e5-bc51-c22b8e21c47a@@true@@ACMESPACE:null@@{}\"></jahia_esi:include>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    \n" +
                "    </div>\n" +
                "</div><!-- jahia:temp value=\"URLParserEnd882f6c25\" -->";

        assertEquals(replaced, strategy.replace(initial));

        initial = "<!-- jahia:temp value=\"URLParserStart0bf1b783\" --><!-- cache:include " +
                "src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@db6c9586-ea04-48cf-8fc3-4a6343dce5f8@@@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart1c8256b1\" --><section class=\"illustration-section\" >\n" +
                "    <div class=\"container-fluid \">\n" +
                "        <div class=\"flexslider carousel\">\n" +
                "            <ul class=\"slides\">\n" +
                "                \n" +
                "                    <li>\n" +
                "                        <!-- cache:include src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel/spacecarouselitem@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel%2Fspacecarouselitem@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@762a7c73-705f-4102-b5d1-c686b15e8604@@@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStartd78fe240\" --><div class=\"container\">\n" +
                "            <div class=\"carousel-caption\">\n" +
                "\n" +
                "                <h2>Welcome to the Space Community</h2>\n" +
                "\n" +
                "                <p class=\"lead inverse\">Acme Space created a huge community around its activities and is currently interacting with an impressive number of passionate people thanks to valuable content produced by Acme's engineering teams.</p>\n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/community.html\" class=\"btn btn-large btn-primary\"><i class=\"fa fa-chevron-right\"></i>\n" +
                "                    Read more\n" +
                "                </a>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"carousel-img\">\n" +
                "                \n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/community.html\"><img src=\"/jahia/files/live/sites/ACMESPACE/files/Images/Banner-home-slider/banner-earth.png\" alt=\"banner-earth.png\"/></a>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "            </div>\n" +
                "        </div><!-- jahia:temp value=\"URLParserEndd78fe240\" -->\n" +
                "<!-- /cache:include -->\n" +
                "                    </li>\n" +
                "                \n" +
                "                    <li>\n" +
                "                        <!-- cache:include src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel/sky-is-not-the-limit@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel%2Fsky-is-not-the-limit@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@25fdc785-a0b1-4516-9c59-f9b8a0c7de22@@@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart8d76cc7b\" --><div class=\"container\">\n" +
                "            <div class=\"carousel-caption\">\n" +
                "\n" +
                "                <h2>Sky is not the limit</h2>\n" +
                "\n" +
                "                <p class=\"lead inverse\">Acme Space is the largest space private group, with more than 40,000 employees around the world, deploying its expertise across the full spectrum of space projects, includes space launch, commercial satellites, government satellites lines of business.</p>\n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/about-us.html\" class=\"btn btn-large btn-primary\"><i class=\"fa fa-chevron-right\"></i>\n" +
                "                    Read more\n" +
                "                </a>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"carousel-img\">\n" +
                "                \n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/about-us.html\"><img src=\"/jahia/files/live/sites/ACMESPACE/files/Images/Banner-home-slider/banner-astronaut.png\" alt=\"banner-astronaut.png\"/></a>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "            </div>\n" +
                "        </div><!-- jahia:temp value=\"URLParserEnd8d76cc7b\" -->\n" +
                "<!-- /cache:include -->\n" +
                "                    </li>\n" +
                "                \n" +
                "                    <li>\n" +
                "                        <!-- cache:include src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel/rock-solid-work@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel%2Frock-solid-work@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@486af1dc-aebf-4c2b-ba6f-cd804347e221@@@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart7e3bd97f\" --><div class=\"container\">\n" +
                "            <div class=\"carousel-caption\">\n" +
                "\n" +
                "                <h2>Rock Solid Work</h2>\n" +
                "\n" +
                "                <p class=\"lead inverse\">Acme Space is the largest space private group, with more than 40,000 employees around the world, deploying its expertise across the full spectrum of space projects, includes space launch, commercial satellites, government satellites lines of business.</p>\n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/activities.html\" class=\"btn btn-large btn-primary\"><i class=\"fa fa-chevron-right\"></i>\n" +
                "                    Read more\n" +
                "                </a>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"carousel-img\">\n" +
                "                \n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/activities.html\"><img src=\"/jahia/files/live/sites/ACMESPACE/files/Images/Banner-home-slider/banner-satellite.png\" alt=\"banner-satellite.png\"/></a>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "            </div>\n" +
                "        </div><!-- jahia:temp value=\"URLParserEnd7e3bd97f\" -->\n" +
                "<!-- /cache:include -->\n" +
                "                    </li>\n" +
                "                \n" +
                "                    <li>\n" +
                "                        <!-- cache:include src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel/failure-is-not-an-option@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel%2Ffailure-is-not-an-option@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@41e16dac-4404-4620-b592-452c5f7f9784@@@@ACMESPACE:null@@{}\" -->\n" +
                "<!-- jahia:temp value=\"URLParserStart3fea054c\" --><div class=\"container\">\n" +
                "            <div class=\"carousel-caption\">\n" +
                "\n" +
                "                <h2>Failure Is Not An Option</h2>\n" +
                "\n" +
                "                <p class=\"lead inverse\">When Acme Space's engineers are in charge of a space projects, they work to succeed because there is no other alternative. It's their DNA. Discover their results in our news section.</p>\n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/news.html\" class=\"btn btn-large btn-primary\"><i class=\"fa fa-chevron-right\"></i>\n" +
                "                    Read more\n" +
                "                </a>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"carousel-img\">\n" +
                "                \n" +
                "                <a href=\"/jahia/sites/ACMESPACE/home/news.html\"><img src=\"/jahia/files/live/sites/ACMESPACE/files/Images/Banner-home-slider/banner-shuttle.png\" alt=\"banner-shuttle.png\"/></a>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "            </div>\n" +
                "        </div><!-- jahia:temp value=\"URLParserEnd3fea054c\" -->\n" +
                "<!-- /cache:include -->\n" +
                "                    </li>\n" +
                "                \n" +
                "            </ul>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "</section>\n" +
                "<jahia:resource type=\"css\" path=\"%2Fjahia%2Fmodules%2Fbootstrap-acme-space-templates%2Fcss%2Fflexslider.css\" insert=\"false\" media=\"screen\" resource=\"flexslider.css\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fjquery%2Fjavascript%2Fjquery.min.js\" insert=\"false\" resource=\"jquery.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fassets%2Fjavascript%2Fjquery.jahia.min.js\" insert=\"false\" resource=\"jquery.jahia.min.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"javascript\" path=\"%2Fjahia%2Fmodules%2Fbootstrap-acme-space-templates%2Fjavascript%2Fjquery.flexslider.js\" insert=\"false\" resource=\"jquery.flexslider.js\" title=\"\" key=\"\" />\n" +
                "<jahia:resource type=\"inline\" path=\"%0A++++++++%3Cscript%3E%0A++++++++++++%2F%2F+Can+also+be+used+with+%24%28window%29.load%0A++++++++++++%24%28document%29.ready%28function+%28%29+%7B%0A++++++++++++++++%24%28%27.flexslider%27%29.flexslider%28%7B%0A++++++++++++++++++++animation%3A+%22slide%22%2C%0A++++++++++++++++++++smoothHeight%3A+true%0A++++++++++++++++%7D%29%3B%0A++++++++++++%7D%29%3B%0A++++++++%3C%2Fscript%3E%0A++++\" insert=\"false\" resource=\"\" title=\"\" key=\"\" /><!-- jahia:temp value=\"URLParserEnd1c8256b1\" -->\n" +
                "<!-- /cache:include --><div class=\"clear\"></div><!-- jahia:temp value=\"URLParserEnd0bf1b783\" -->";
        replaced = "<!-- jahia:temp value=\"URLParserStart0bf1b783\" --><jahia_esi:include " +
                "src=\"en@@/sites/ACMESPACE/home/slider-1/acme-space-demo-carousel@@default@@html@@%2Fsites%2FACMESPACE%2Fhome%2Fslider-1%2Facme-space-demo-carousel@@module@@false@@@@_qs[ec, v, cacheinfo, moduleinfo]_@@@@db6c9586-ea04-48cf-8fc3-4a6343dce5f8@@@@ACMESPACE:null@@{}\"></jahia_esi:include><div class=\"clear\"></div><!-- jahia:temp value=\"URLParserEnd0bf1b783\" -->";

        assertEquals(replaced, strategy.replace(initial));
    }

    private void buildTestWithDefaults(String expected, String initial, String... expectedMatches) {
        buildTest(expected, initial, null, PREFIX, SUFFIX, expectedMatches);
    }

    private void buildTest(String expected, String initial, String replacement, String prefix, String suffix, String... expectedMatches) {
        if (replacement == null) {
            replacement = REPLACEMENT;
        }
        TestStringReplacementGenerator gen = new TestStringReplacementGenerator(replacement);
        gen.setExpectedMatches(expectedMatches);

        assertEquals(expected, TextUtils.replaceBoundedString(initial, prefix, suffix, gen));

        CountingVisitor countingVisitor = new CountingVisitor();
        assertEquals(expectedMatches.length, (int) TextUtils.visitBoundedString(initial, prefix, suffix, countingVisitor));

        MatchRecordingVisitor matchRecordingVisitor = new MatchRecordingVisitor();
        final boolean result = Arrays.equals(expectedMatches, TextUtils.visitBoundedString(initial, prefix, suffix, matchRecordingVisitor).toArray(new String[expectedMatches.length]));
        assertTrue(result);
    }

    private static interface ReplacementStrategy {
        String replace(String initial);

        ReplacementStrategy TEXT_UTILS = new ReplacementStrategy() {
            @Override
            public String replace(String initial) {
                return TextUtils.replaceBoundedString(initial, "<!-- cache:include", "/cache:include -->", GENERATOR);
            }
        };

        ReplacementStrategy JERICHO = new ReplacementStrategy() {
            private final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile("<!-- cache:include src=\\\"(.*)\\\" -->");
            private final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");

            @Override
            public String replace(String initial) {
                // Replace <!-- cache:include --> tags of sub fragments by HTML tags that can be parsed by jericho
                String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(initial).replaceAll("</jahia_esi:include>");
                cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll("<jahia_esi:include src=\"$1\">");

                if (cachedRenderContent.contains(CACHE_ESI_TAG_START)) {
                    Source source = new Source(cachedRenderContent);

                    //// This will remove all blank line and drastically reduce data in memory
                    // source = new Source((new SourceFormatter(source)).toString());

                    // We will remove module:tag content here has we do not want to store them twice in memory
                    List<StartTag> esiIncludeTags = source.getAllStartTags("jahia_esi:include");
                    OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);

                    return outputDocument.toString();
                }


                return initial;
            }

            private OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
                OutputDocument outputDocument = new OutputDocument(source);
                for (StartTag segment : segments) {
                    outputDocument.replace(segment.getElement().getContent(), "");
                }
                return outputDocument;
            }
        };
    }

    static class TestStringReplacementGenerator implements TextUtils.ReplacementGenerator {
        private String replacement;
        private String[] expectedMatches;
        private int invocationCount;

        TestStringReplacementGenerator(String replacement) {
            this.replacement = replacement;
        }

        public void setExpectedMatches(String... expectedMatches) {
            this.expectedMatches = expectedMatches;
            this.invocationCount = 0;
        }

        @Override
        public void appendReplacementForMatch(int matchStart, int matchEnd, char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix) {
            if (expectedMatches == null) {
                fail("getReplacementFor shouldn't have been called");
            }
            String expected = expectedMatches[invocationCount++];
            assertEquals("'" + expected + "'", "'" + TextUtils.getStringBetween(initialStringAsCharArray, matchStart, matchEnd) + "'");

            builder.append(replacement);
        }
    }

    static class CountingVisitor implements TextUtils.BoundedStringVisitor<Integer> {
        private int count;

        @Override
        public Integer visit(String prefix, String suffix, int matchStart, int matchEnd, char[] initialStringAsCharArray) {
            count++;
            return count;
        }

        @Override
        public Integer initialValue(String initial) {
            return 0;
        }

        public int getCount() {
            return count;
        }
    }

    static class MatchRecordingVisitor implements TextUtils.BoundedStringVisitor<Collection<String>> {
        private List<String> matches = new LinkedList<>();

        @Override
        public Collection<String> visit(String prefix, String suffix, int matchStart, int matchEnd, char[] initialStringAsCharArray) {
            matches.add(TextUtils.getStringBetween(initialStringAsCharArray, matchStart, matchEnd));
            return matches;
        }

        @Override
        public Collection<String> initialValue(String initial) {
            return Collections.emptyList();
        }
    }

}
