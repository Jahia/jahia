package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.contentmanager.Item;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.toolbar.ToolbarSet;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 2:54:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagerConfiguration implements Serializable, BeanNameAware {

    private String beanName;
    private boolean enableTextMenu;

    private List<Column> tableColumns;
    private List<Repository> accordionPanels;
    private List<EngineTab> tabs;

    private String selectedAccordion = null;
    private boolean hideLeftPanel = false;

    private String folderTypes;
    private String nodeTypes;
    private String filters;
    private String mimeTypes;

    private short defaultView;
    private boolean enableFileDoubleClick = true;
    private boolean displaySize = true;
    private boolean displayExt = true;
    private boolean displayLock = true;
    private boolean displayDate = true;
    private boolean displayProvider = false;
    private boolean useCheckboxForSelection = true;

    private ToolbarSet toolbarSet;


    private boolean expandRoot = false;

    private boolean allowCollections = true;

    private boolean displaySearch = true;

    private boolean displaySearchInPage = true;
    private boolean displaySearchInTag = true;
    private boolean displaySearchInFile = true;
    private boolean displaySearchInContent = true;



    public ManagerConfiguration() {
        tableColumns = new ArrayList<Column>();
        accordionPanels = new ArrayList<Repository>();
        tabs = new ArrayList<EngineTab>();
    }

    public void setSelectedAccordion(String accordion) {
        selectedAccordion = accordion;
    }

    public List<EngineTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<EngineTab> tabs) {
        this.tabs = tabs;
    }

    public void setHideLeftPanel(boolean hide) {
        this.hideLeftPanel = hide;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<Repository> getAccordionPanels() {
        return accordionPanels;
    }

    public void setAccordionPanels(List<Repository> accordionPanels) {
        this.accordionPanels = accordionPanels;
    }

    public String getSelectedAccordion() {
        return selectedAccordion;
    }

    public boolean isHideLeftPanel() {
        return hideLeftPanel;
    }

    public boolean isEnableTextMenu() {
        return enableTextMenu;
    }

    public void setEnableTextMenu(boolean enableTextMenu) {
        this.enableTextMenu = enableTextMenu;
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

    public short getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(short defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isEnableFileDoubleClick() {
        return enableFileDoubleClick;
    }

    public void setEnableFileDoubleClick(boolean enableFileDoubleClick) {
        this.enableFileDoubleClick = enableFileDoubleClick;
    }

    public boolean isDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(boolean displaySize) {
        this.displaySize = displaySize;
    }

    public boolean isDisplayExt() {
        return displayExt;
    }

    public void setDisplayExt(boolean displayExt) {
        this.displayExt = displayExt;
    }

    public boolean isDisplayLock() {
        return displayLock;
    }

    public void setDisplayLock(boolean displayLock) {
        this.displayLock = displayLock;
    }

    public boolean isDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(boolean displayDate) {
        this.displayDate = displayDate;
    }

    public boolean isDisplayProvider() {
        return displayProvider;
    }

    public void setDisplayProvider(boolean displayProvider) {
        this.displayProvider = displayProvider;
    }

    public boolean isAllowCollections() {
        return allowCollections;
    }

    public void setAllowCollections(boolean allowConnections) {
        this.allowCollections = allowConnections;
    }


    public ToolbarSet getToolbarSet() {
        return toolbarSet;
    }

    public void setToolbarSet(ToolbarSet toolbarSet) {
        this.toolbarSet = toolbarSet;
    }

    public boolean isUseCheckboxForSelection() {
        return useCheckboxForSelection;
    }

    public void setUseCheckboxForSelection(boolean useCheckboxForSelection) {
        this.useCheckboxForSelection = useCheckboxForSelection;
    }

    public boolean isExpandRoot() {
        return expandRoot;
    }

    public void setExpandRoot(boolean expandRoot) {
        this.expandRoot = expandRoot;
    }

    public boolean isDisplaySearch() {
        return displaySearch;
    }

    public void setDisplaySearch(boolean displaySearch) {
        this.displaySearch = displaySearch;
    }

    public boolean isDisplaySearchInPage() {
        return displaySearchInPage;
    }

    public void setDisplaySearchInPage(boolean displaySearchInPage) {
        this.displaySearchInPage = displaySearchInPage;
    }

    public boolean isDisplaySearchInTag() {
        return displaySearchInTag;
    }

    public void setDisplaySearchInTag(boolean displaySearchInTag) {
        this.displaySearchInTag = displaySearchInTag;
    }

    public boolean isDisplaySearchInFile() {
        return displaySearchInFile;
    }

    public void setDisplaySearchInFile(boolean displaySearchInFile) {
        this.displaySearchInFile = displaySearchInFile;
    }

    public boolean isDisplaySearchInContent() {
        return displaySearchInContent;
    }

    public void setDisplaySearchInContent(boolean displaySearchInContent) {
        this.displaySearchInContent = displaySearchInContent;
    }



    public void setBeanName(String name) {
        beanName = name;
    }
}
