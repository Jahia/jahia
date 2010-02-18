package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 16, 2010
 * Time: 4:19:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaSearchQuery implements Serializable {
    private int limit;
    private String query;
    private List<GWTJahiaNode> pages;
    private GWTJahiaLanguage language;
    private boolean inName;
    private boolean inTags;
    private boolean inContents;
    private boolean inFiles;
    private boolean inMetadatas;
    private String folderTypes;
    private String nodeTypes;
    private String filters;
    private String mimeTypes;
    
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<GWTJahiaNode> getPages() {
        return pages;
    }

    public void setPages(List<GWTJahiaNode> pages) {
        this.pages = pages;
    }

    public GWTJahiaLanguage getLanguage() {
        return language;
    }

    public void setLanguage(GWTJahiaLanguage language) {
        this.language = language;
    }

    public boolean isInName() {
        return inName;
    }

    public void setInName(boolean inName) {
        this.inName = inName;
    }

    public boolean isInTags() {
        return inTags;
    }

    public void setInTags(boolean inTags) {
        this.inTags = inTags;
    }

    public boolean isInContents() {
        return inContents;
    }

    public void setInContents(boolean inContents) {
        this.inContents = inContents;
    }

    public boolean isInFiles() {
        return inFiles;
    }

    public void setInFiles(boolean inFiles) {
        this.inFiles = inFiles;
    }

    public boolean isInMetadatas() {
        return inMetadatas;
    }

    public void setInMetadatas(boolean inMetadatas) {
        this.inMetadatas = inMetadatas;
    }

    public String getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(String folderTypes) {
        this.folderTypes = folderTypes;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }
}
