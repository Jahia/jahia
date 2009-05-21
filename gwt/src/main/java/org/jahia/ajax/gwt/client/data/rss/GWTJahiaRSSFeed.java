/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.rss;

import java.io.Serializable;
import java.util.List;


/**
 * User: jahia
 * Date: 13 ao�t 2008
 * Time: 12:32:06
 */
public class GWTJahiaRSSFeed implements Serializable {
    private String url;
    private int nbDisplayedEntries;
    private String title;
    private String author;
    private List<String> authors;
    private List<String> categories;
    private List<String> contributors;
    private String copyright;
    private String description;
    private String encoding;
    private List<GWTJahiaRSSEntry> entries;
    private String feedType;
    private GWTJahiaRSSImage image;
    private String language;

    public GWTJahiaRSSFeed() {
    }

    public GWTJahiaRSSFeed(String url, String title,String author, List<String> authors, List<String> categories, List<String> contributors, String copyright, String description, String encoding, List<GWTJahiaRSSEntry> entries, String feedType, GWTJahiaRSSImage image, String language) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.authors = authors;
        this.categories = categories;
        this.contributors = contributors;
        this.copyright = copyright;
        this.description = description;
        this.encoding = encoding;
        this.entries = entries;
        this.feedType = feedType;
        this.image = image;
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNbDisplayedEntries() {
        return nbDisplayedEntries;
    }

    public void setNbDisplayedEntries(int nbDisplayedEntries) {
        this.nbDisplayedEntries = nbDisplayedEntries;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<GWTJahiaRSSEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GWTJahiaRSSEntry> entries) {
        this.entries = entries;
    }


    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public GWTJahiaRSSImage getImage() {
        return image;
    }

    public void setImage(GWTJahiaRSSImage image) {
        this.image = image;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}