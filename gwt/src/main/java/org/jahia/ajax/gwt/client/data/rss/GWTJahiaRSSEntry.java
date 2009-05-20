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
package org.jahia.ajax.gwt.client.data.rss;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.util.Date;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 13 ao�t 2008
 * Time: 14:55:18
 */
public class GWTJahiaRSSEntry extends BaseModel implements Serializable {
    public GWTJahiaRSSEntry() {
    }

    public GWTJahiaRSSEntry(String author, List<GWTJahiaRSSPerson> authors, List<GWTJahiaRSSCategory> categories, List<GWTJahiaRSSContent> contents,
                       List<GWTJahiaRSSPerson> contributors, String description, List<GWTJahiaRSSEnclosure> enclosures, String link,
                       List<String> links,
                       Date publishedDate, String title, Date updatedDate) {
        setAuthor(author);
        setAuthors(authors);
        setCategories(categories);
        setContents(contents);
        setContributors(contributors);
        setDescription(description);
        setShortDescription(getDescription());
        setEnclosures(enclosures);
        setLink(link);
        setLinks(links);
        setPublishedDate(publishedDate);
        setTitle(title);
        setUpdatedDate(updatedDate);
        set("authorLabel", "Author");
        set("publishedDateLabel", "Publish date");
        set("updatedDateLabel", "Updated date");
    }

    public String getAuthor() {
        return get("author");
    }

    public void setAuthor(String author) {
        set("author", author);
    }

    public List<GWTJahiaRSSPerson> getAuthors() {
        return get("authors");
    }

    public void setAuthors(List<GWTJahiaRSSPerson> authors) {
        set("authors", authors);
    }

    public List<GWTJahiaRSSCategory> getCategories() {
        return get("categories");
    }

    public void setCategories(List<GWTJahiaRSSCategory> categories) {
        set("categories", categories);
    }

    public List<GWTJahiaRSSContent> getContents() {
        return get("contents");
    }

    public void setContents(List<GWTJahiaRSSContent> contents) {
        this.set("contents", contents);
    }

    public List<GWTJahiaRSSPerson> getContributors() {
        return get("contributors");
    }

    public void setContributors(List<GWTJahiaRSSPerson> contributors) {
        set("contributors", contributors);
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }

    public String getShortDescription() {
        return get("shortDescription");
    }

    public void setShortDescription(String shortDescription) {
        set("shortDescription", shortDescription);
    }

    public List<GWTJahiaRSSEnclosure> getEnclosures() {
        return get("enclosures");
    }

    public void setEnclosures(List<GWTJahiaRSSEnclosure> enclosures) {
        set("enclosures", enclosures);
    }

    public String getLink() {
        return get("link");
    }

    public void setLink(String link) {
        set("link", link);
    }

    public List<String> getLinks() {
        return get("links");
    }

    public void setLinks(List<String> links) {
        set("links", links);
    }

    public Date getPublishedDate() {
        return get("publishedDate");
    }

    public void setPublishedDate(Date publishedDate) {
        set("publishedDate", publishedDate);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String title) {
        set("title", title);
    }

    public Date getUpdatedDate() {
        return get("updatedDate");
    }

    public void setUpdatedDate(Date updatedDate) {
        set("updatedDate", updatedDate);
    }
}