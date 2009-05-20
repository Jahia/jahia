/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.utils;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.FeedFetcher;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.net.URL;
import java.io.IOException;

import org.jahia.ajax.gwt.client.data.rss.*;
import org.apache.log4j.Logger;

/**
 * User: ktlili
 * Date: 19 nov. 2008
 * Time: 11:22:57
 */
public class RSSHelper {
    private static final transient Logger logger = Logger.getLogger(RSSHelper.class);

    public static GWTJahiaRSSFeed createGWTRSSFeed(URL feedUrl) {
        boolean reformatGoogleNewsEntry = false;
        SyndFeed feed = null;
        try {
            final SyndFeedInput input = new SyndFeedInput();
            final XmlReader reader = new XmlReader(feedUrl.openStream());
            feed = input.build(reader);
        } catch (FeedException e) {
            logger.error(e.getMessage() + ", feedException --> Can't load rss.");
        } catch (IOException e) {
            // another way to load feed RSS
            final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
            final FeedFetcher fetcher = new HttpURLFeedFetcher(feedInfoCache);
            try {
                feed = fetcher.retrieveFeed(feedUrl);
            } catch (Exception e1) {
                logger.error(e.getMessage() + ", exception --> Can't load rss.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + ", exception --> Can't load rss.");
        }
        if (feed == null) {
            return null;
        }
        
        // handle goole news
        if (feedUrl != null && "news.google.com".equals(feedUrl.getHost())) {
            reformatGoogleNewsEntry = true;
        }

        String url = feed.getUri();
        String title = feed.getTitle();
        String author = feed.getAuthor();
        List<String> authors = null;//feed.getAuthors();
        List<String> categories = null;//feed.getCategories();
        List<String> contributors = null;//feed.getContributors();
        String copyright = feed.getCopyright();
        String description = feed.getDescription();
        String encoding = feed.getEncoding();
        List<GWTJahiaRSSEntry> entries = createGWTRSSEntries(reformatGoogleNewsEntry, feed.getEntries());
        String feedType = feed.getFeedType();
        GWTJahiaRSSImage gwtrssImage = createGWTRSSImage(feed.getImage());
        String language = feed.getLanguage();
        return new GWTJahiaRSSFeed(url, title, author, authors, categories, contributors, copyright, description, encoding, entries, feedType, gwtrssImage, language);

    }

    private static List<GWTJahiaRSSEntry> createGWTRSSEntries(boolean reformatGoogleNewsEntry, List<SyndEntry> syndEntryList) {
        if (syndEntryList == null) {
            return null;
        }
        List<GWTJahiaRSSEntry> gwtrssEntries = new ArrayList<GWTJahiaRSSEntry>();

        for (SyndEntry syndEntry : syndEntryList) {
            if (reformatGoogleNewsEntry) {
                reformatGoogleNewsEntry(syndEntry);
            }
            String author = syndEntry.getAuthor();

            // authors
            List<GWTJahiaRSSPerson> authors = new ArrayList<GWTJahiaRSSPerson>();
            if (syndEntry.getAuthors() != null) {
                for (Object syndPersonObj : syndEntry.getAuthors()) {
                    GWTJahiaRSSPerson gwtrssPerson = createGWTRSSPerson((SyndPerson) syndPersonObj);
                    authors.add(gwtrssPerson);
                }
            }

            // categories
            List<GWTJahiaRSSCategory> categories = new ArrayList<GWTJahiaRSSCategory>();
            /*
            Not handled due to a bug with gwt ---> to be fixed
            if (syndEntry.getCategories() != null) {
                for (Object syndCategoryObj : syndEntry.getCategories()) {
                    GWTJahiaRSSCategory gwtrssCategory = createGWTRSSCategory((SyndCategory) syndCategoryObj);
                    categories.add(gwtrssCategory);
                }
            } */

            // SynContent list
            List<GWTJahiaRSSContent> contents = new ArrayList<GWTJahiaRSSContent>();
            if (syndEntry.getContents() != null) {
                for (Object syndContentObj : syndEntry.getContents()) {
                    GWTJahiaRSSContent gwtrssContent = createGWTRSSContent((SyndContent) syndContentObj);
                    contents.add(gwtrssContent);
                }
            }

            // contibutors
            List<GWTJahiaRSSPerson> contributors = new ArrayList<GWTJahiaRSSPerson>();
            if (syndEntry.getContributors() != null) {
                for (Object syndPersonObj : syndEntry.getContributors()) {
                    GWTJahiaRSSPerson gwtrssPerson = createGWTRSSPerson((SyndPerson) syndPersonObj);
                    contributors.add(gwtrssPerson);
                }
            }
            String description = null;
            if (syndEntry.getDescription() != null) {
                description = syndEntry.getDescription().getValue();
            }

            // description
            if (description == null) {
                description = "";
            }

            // enclosure
            List<GWTJahiaRSSEnclosure> enclosures = null;//new ArrayList<GWTJahiaRSSEnclosure>();
            /*
            Not handled due to a bug with gwt ---> to be fixed
            if (syndEntry.getEnclosures() != null) {
                for (Object syndEnclosureObj : syndEntry.getEnclosures()) {
                    GWTJahiaRSSEnclosure gwtrssEnclosure = createGWTRSSEnclosure((SyndEnclosure) syndEnclosureObj);
                    enclosures.add(gwtrssEnclosure);
                }
            } */
            String link = syndEntry.getLink();
            List<String> links = null;//syndEntry.getLinks();
            Date publishedDate = null;//syndEntry.getPublishedDate();
            String title = syndEntry.getTitle();
            Date updatedDate = null;//syndEntry.getUpdatedDate();
            gwtrssEntries.add(new GWTJahiaRSSEntry(author, authors, categories, contents, contributors, description, enclosures, link, links, publishedDate, title, updatedDate));
        }
        return gwtrssEntries;
    }

    /**
     * @param syndPerson
     * @return
     */
    private static GWTJahiaRSSPerson createGWTRSSPerson(SyndPerson syndPerson) {
        GWTJahiaRSSPerson gwtrssPerson = new GWTJahiaRSSPerson();
        gwtrssPerson.setEmail(syndPerson.getEmail());
        gwtrssPerson.setName(syndPerson.getName());
        gwtrssPerson.setUri(syndPerson.getUri());
        return gwtrssPerson;
    }

    /**
     * @param syndContent
     * @return
     */
    private static GWTJahiaRSSContent createGWTRSSContent(SyndContent syndContent) {
        GWTJahiaRSSContent gwtrssContent = new GWTJahiaRSSContent();
        gwtrssContent.setMode(syndContent.getMode());
        gwtrssContent.setValue(syndContent.getValue());
        gwtrssContent.setType(syndContent.getType());
        return gwtrssContent;
    }

    /**
     * @param syndEnclosure
     * @return
     */
    private static GWTJahiaRSSEnclosure createGWTRSSEnclosure(SyndEnclosure syndEnclosure) {
        GWTJahiaRSSEnclosure gwtrssEnclosure = new GWTJahiaRSSEnclosure();
        gwtrssEnclosure.setUrl(syndEnclosure.getUrl());
        gwtrssEnclosure.setType(syndEnclosure.getType());
        //  gwtrssEnclosure.setLength(syndEnclosure.getLength());
        return gwtrssEnclosure;
    }

    /**
     * Create a GWTRSSImage from an syndImage
     *
     * @param syndImage
     * @return
     */
    public static GWTJahiaRSSImage createGWTRSSImage(SyndImage syndImage) {
        if (syndImage == null) {
            return null;
        }
        String description = syndImage.getDescription();
        String link = syndImage.getLink();
        String title = syndImage.getTitle();
        String url = syndImage.getUrl();

        return new GWTJahiaRSSImage(description, link, title, url);
    }

    /**
     * Reaformat codes coming from google news
     * @param entry
     */
    private static void reformatGoogleNewsEntry(SyndEntry entry) {
        final SyndContent entryDescription = entry.getDescription();
        if (entryDescription != null) {
            final String description = entryDescription.getValue();
            final int descBegin = description.lastIndexOf("<font size=-1>") + 14;
            final int descEnd = description.indexOf("</font>", descBegin);

            if (descBegin != -1 && descEnd != -1) {
                final String realDesc = description.substring(descBegin, descEnd);
                entryDescription.setValue(realDesc);
            }
        }
    }
}
