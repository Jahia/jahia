/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.utils;

import com.sun.syndication.feed.synd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.jahia.ajax.gwt.commons.client.beans.rss.*;
import org.apache.log4j.Logger;

/**
 * User: ktlili
 * Date: 19 nov. 2008
 * Time: 11:22:57
 */
public class RSSHelper {
    private static final transient Logger logger = Logger.getLogger(RSSHelper.class);

    public static GWTRSSFeed createGWTRSSFeed(SyndFeed feed) {
        if (feed == null) {
            return null;
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
        List<GWTRSSEntry> entries = createGWTRSSEntries(feed.getEntries());
        String feedType = feed.getFeedType();
        GWTRSSImage gwtrssImage = createGWTRSSImage(feed.getImage());
        String language = feed.getLanguage();
        return new GWTRSSFeed(url, title, author, authors, categories, contributors, copyright, description, encoding, entries, feedType, gwtrssImage, language);

    }

    private static List<GWTRSSEntry> createGWTRSSEntries(List<SyndEntry> syndEntryList) {
        if (syndEntryList == null) {
            return null;
        }
        List<GWTRSSEntry> gwtrssEntries = new ArrayList<GWTRSSEntry>();

        for (SyndEntry syndEntry : syndEntryList) {
            String author = syndEntry.getAuthor();

            // authors
            List<GWTRSSPerson> authors = new ArrayList<GWTRSSPerson>();
            if (syndEntry.getAuthors() != null) {
                for (Object syndPersonObj : syndEntry.getAuthors()) {
                    GWTRSSPerson gwtrssPerson = createGWTRSSPerson((SyndPerson) syndPersonObj);
                    authors.add(gwtrssPerson);
                }
            }

            // categories
            List<GWTRSSCategory> categories = new ArrayList<GWTRSSCategory>();
            /*
            Not handled due to a bug with gwt ---> to be fixed
            if (syndEntry.getCategories() != null) {
                for (Object syndCategoryObj : syndEntry.getCategories()) {
                    GWTRSSCategory gwtrssCategory = createGWTRSSCategory((SyndCategory) syndCategoryObj);
                    categories.add(gwtrssCategory);
                }
            } */

            // SynContent list
            List<GWTRSSContent> contents = new ArrayList<GWTRSSContent>();
            if (syndEntry.getContents() != null) {
                for (Object syndContentObj : syndEntry.getContents()) {
                    GWTRSSContent gwtrssContent = createGWTRSSContent((SyndContent) syndContentObj);
                    contents.add(gwtrssContent);
                }
            }

            // contibutors
            List<GWTRSSPerson> contributors = new ArrayList<GWTRSSPerson>();
            if (syndEntry.getContributors() != null) {
                for (Object syndPersonObj : syndEntry.getContributors()) {
                    GWTRSSPerson gwtrssPerson = createGWTRSSPerson((SyndPerson) syndPersonObj);
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
            List<GWTRSSEnclosure> enclosures = null;//new ArrayList<GWTRSSEnclosure>();
            /*
            Not handled due to a bug with gwt ---> to be fixed
            if (syndEntry.getEnclosures() != null) {
                for (Object syndEnclosureObj : syndEntry.getEnclosures()) {
                    GWTRSSEnclosure gwtrssEnclosure = createGWTRSSEnclosure((SyndEnclosure) syndEnclosureObj);
                    enclosures.add(gwtrssEnclosure);
                }
            } */
            String link = syndEntry.getLink();
            List<String> links = null;//syndEntry.getLinks();
            Date publishedDate = null;//syndEntry.getPublishedDate();
            String title = syndEntry.getTitle();
            Date updatedDate = null;//syndEntry.getUpdatedDate();
            gwtrssEntries.add(new GWTRSSEntry(author, authors, categories, contents, contributors, description, enclosures, link, links, publishedDate, title, updatedDate));
        }
        return gwtrssEntries;
    }

    /**
     * @param syndPerson
     * @return
     */
    private static GWTRSSPerson createGWTRSSPerson(SyndPerson syndPerson) {
        GWTRSSPerson gwtrssPerson = new GWTRSSPerson();
        gwtrssPerson.setEmail(syndPerson.getEmail());
        gwtrssPerson.setName(syndPerson.getName());
        gwtrssPerson.setUri(syndPerson.getUri());
        return gwtrssPerson;
    }

    /**
     * @param syndContent
     * @return
     */
    private static GWTRSSContent createGWTRSSContent(SyndContent syndContent) {
        GWTRSSContent gwtrssContent = new GWTRSSContent();
        gwtrssContent.setMode(syndContent.getMode());
        gwtrssContent.setValue(syndContent.getValue());
        gwtrssContent.setType(syndContent.getType());
        return gwtrssContent;
    }

    /**
     * @param syndEnclosure
     * @return
     */
    private static GWTRSSEnclosure createGWTRSSEnclosure(SyndEnclosure syndEnclosure) {
        GWTRSSEnclosure gwtrssEnclosure = new GWTRSSEnclosure();
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
    public static GWTRSSImage createGWTRSSImage(SyndImage syndImage) {
        if (syndImage == null) {
            return null;
        }
        String description = syndImage.getDescription();
        String link = syndImage.getLink();
        String title = syndImage.getTitle();
        String url = syndImage.getUrl();

        return new GWTRSSImage(description, link, title, url);
    }
}
