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

package org.jahia.syndication.opensearch;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.feed.module.opensearch.OpenSearchModule;
import com.sun.syndication.feed.module.opensearch.impl.OpenSearchModuleImpl;
import com.sun.syndication.feed.module.opensearch.entity.OSQuery;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.SyndFeedOutput;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.Writer;
import java.io.FileWriter;

import org.jahia.params.ParamBean;
import org.jahia.services.search.SearchResponse;
import org.jahia.utils.DateUtils;

/**
 * RSS feed producer for the Jahia search provider.
 * 
 * @author Khue NGuyen
 * Date: 6 oct. 2007
 * Time: 10:27:42
 */
public class FeedWriter {

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (FeedWriter.class);

    private static final DateFormat DATE_PARSER = new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT);

    public FeedWriter(){
    }

    public void write(String feedType,
                      ParamBean jParams,
                      SearchResponse searchResult,
                      String searchString,
                      Writer writer){
        try {
            // feed source link
            StringBuffer feedLink = new StringBuffer();
            feedLink.append(jParams.getScheme());
            feedLink.append("://");
            feedLink.append(jParams.getServerName());
            feedLink.append(":");
            feedLink.append(jParams.getServerPort());

            String serverURL = feedLink.toString();

            SyndFeed feed = new SyndFeedImpl();

            setFeedModules(feed,searchResult,searchString);

            feed.setFeedType(feedType);
            feed.setTitle("Jahia CMS Search Result Feed");
            feed.setLink(feedLink.toString());
            feed.setDescription("Jahia CMS Search Result Feed");
            List<SyndEntry> entries = new ArrayList<SyndEntry>();

//            for ( JahiaSearchHit hit : searchResult.results() ){
//                try {
// TODO: Implement new OpenSearch result fetching
//                     SyndEntry entry = searchService.getSyndEntry(hit,jParams,serverURL);
//                    if (entry != null){
//                        entries.add(entry);
//                    }
//                } catch ( Exception e ){
//                    logger.warn(
//                            "Exception occured creating SyndEntry from hit "
//                                    + hit.getURL() + ". Cause: "
//                                    + e.getMessage(), e);
//                }
//            }
            feed.setEntries(entries);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed,writer);
            writer.close();
        }
        catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param feed
     * @param searchResult
     * @param searchString
     */
    private void setFeedModules(SyndFeed feed, SearchResponse searchResult, String searchString) {

        OpenSearchModuleImpl osm = new OpenSearchModuleImpl();

        osm.setStartIndex(1);
        osm.setTotalResults(searchResult.getResults().size());
        osm.setItemsPerPage(50);

        Link link = new Link();
        link.setHref("http://www.jahia.org/opensearch-description.xml");
        link.setType("application/opensearchdescription+xml");
        link.setTitle("Jahia Open Search");
        osm.setLink(link);

        OSQuery query = new OSQuery();
        query.setRole("request");
        query.setSearchTerms(searchString);
        osm.addQuery(query);

        List<OpenSearchModule> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);
    }

    public static void main(String[] args) {
        boolean ok = false;
        if (args.length==2) {
            try {
                String feedType = args[0];
                String fileName = args[1];

                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType(feedType);

                feed.setTitle("Sample Feed (created with Rome)");
                feed.setLink("http://rome.dev.java.net");
                feed.setDescription("This feed has been created using Rome (Java syndication utilities");
                List<SyndEntry> entries = new ArrayList<SyndEntry>();
                SyndEntry entry;
                SyndContent description;

                entry = new SyndEntryImpl();
                entry.setTitle("Rome v1.0");
                entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome01");
                entry.setPublishedDate(DATE_PARSER.parse("2004-06-08"));
                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue("Initial release of Rome");
                entry.setDescription(description);
                entries.add(entry);

                entry = new SyndEntryImpl();
                entry.setTitle("Rome v2.0");
                entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome02");
                entry.setPublishedDate(DATE_PARSER.parse("2004-06-16"));
                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue("Bug fixes, minor API changes and some new features");
                entry.setDescription(description);
                entries.add(entry);

                entry = new SyndEntryImpl();
                entry.setTitle("Rome v3.0");
                entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome03");
                entry.setPublishedDate(DATE_PARSER.parse("2004-07-27"));
                description = new SyndContentImpl();
                description.setType("text/html");
                description.setValue("<p>More Bug fixes, mor API changes, some new features and some Unit testing</p>"+
                                     "<p>For details check the <a href=\"http://wiki.java.net/bin/view/Javawsxml/RomeChangesLog#RomeV03\">Changes Log</a></p>");
                entry.setDescription(description);
                entries.add(entry);

                feed.setEntries(entries);

                Writer writer = new FileWriter(fileName);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed,writer);
                writer.close();

                System.out.println("The feed has been written to the file ["+fileName+"]");

                ok = true;
            }
            catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        if (!ok) {
            System.out.println();
            System.out.println("FeedWriter creates a RSS/Atom feed and writes it to a file.");
            System.out.println("The first parameter must be the syndication format for the feed");
            System.out.println("  (rss_0.90, rss_0.91, rss_0.92, rss_0.93, rss_0.94, rss_1.0 rss_2.0 or atom_0.3)");
            System.out.println("The second parameter must be the file name for the feed");
            System.out.println();
        }
    }

}
