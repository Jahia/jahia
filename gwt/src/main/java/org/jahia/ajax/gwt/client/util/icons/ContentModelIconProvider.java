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
package org.jahia.ajax.gwt.client.util.icons;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Icon provider for for different types of content objects.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ContentModelIconProvider implements ModelIconProvider<GWTJahiaNode> {

    public static final ContentIconsImageBundle CONTENT_ICONS = GWT.create(ContentIconsImageBundle.class);

    public static final String CONTENT = "icon-content";

    public static final String DIR = "icon-dir";

    public static final String DOC = "icon-doc";

    public static final String EXE = "icon-exe";

    public static final String FILE = "icon-file";

    public static final String GEAR = "icon-gearth";

    public static final String HTML = "icon-html";

    public static final String IMG = "icon-img";

    public static final String LIST = "icon-list";

    public static final String MASHUP = "icon-mashup";

    public static final String PAGE = "icon-page";

    public static final String PDF = "icon-pdf";

    public static final String PLACE_HOLDER = "icon-placeholder";

    public static final String PORTLET = "icon-portlet";

    public static final String PPT = "icon-ppt";

    public static final String RAR = "icon-rar";

    public static final String SOUND = "icon-sound";

    public static final String TXT = "icon-txt";

    public static final String USER_GROUP = "icon-user-group";

    public static final String USER = "icon-user";

    public static final String VIDEO = "icon-video";

    public static final String XLS = "icon-xls";

    public static final String ZIP = "icon-zip";

    public static final String LOCK = "lock";

    public static final String QUERY = "icon-query";
    
    private static final String SEARCH = "icon-searchformcontent";

    private static final String SANDBOX = "icon-sandbox";

    private static final String TEMPLATE = "icon-template";

    public static final String INTERACTIVE = "icon-interactive";

    public static final String STRUCTURED = "icon-structured";

    public static final String CONTENTLIST = "icon-contentlist";

    public static final String FORMCONTENT = "icon-formcontent";


    public static final String DEFAULT_NODE = "default_node";
    public static final String FOLDER_CLOSE = "folder_close";
    public static final String FOLDER_OPEN = "folder_open";
    public static final String JNT_ADDRESS = "jnt_address";
    public static final String JAHIA_FORUM = "jahiaForum:";
    public static final String JNT_RICHTEXT = "jnt:richtext";
    public static final String JNT_VIDEO = "jnt:video";
    public static final String JNT_TEXT = "jnt:text";
    public static final String JNT_FOLDER = "jnt:folder";
    public static final String JNT_FORM = "jnt:form";
    public static final String JNT_IMAGE = "jnt:image";
    public static final String JNT_MAIL = "jnt:mail";
    public static final String JNT_PUBLICATION = "jnt:publication";
    public static final String JNT_TAG = "jnt:tag";
    public static final String JNT_TAG_CLOUD = "jnt:tagCloud";
    public static final String JNT_PAGE_TAGGING = "jnt:pageTagging";
    public static final String JNT_CATEGORY = "jnt:category";
    public static final String JNT_NEWS = "jnt:news";
    public static final String JNT_PIECHART = "jnt:piechart";
    public static final String JNT_FAQ = "jnt:faq";
    public static final String JNT_BOOKMARK = "jnt_bookmark";
    public static final String JNT_SITE = "jnt:site";
    public static final String JNT_INTERVIEW = "jnt:interview";
    public static final String JNT_COMMENT = "jnt:comment";
    public static final String JNT_BLOGPOST = "jnt:blogpost";
    public static final String JNT_EVENT = "jnt:event";
    public static final String JNT_PEOPLE = "jnt:people";
    public static final String PERCENT = "percent";
    public static final String PLUSROUND = "plusround";
    public static final String MINUSROUND = "minusround";
    public static final String JNT_FIELDSET = "jnt:fieldset";
    public static final String JNT_INPUT_TEXT = "jnt:inputText";
    public static final String JNT_INPUT_MULTIPLE = "jnt:inputMultiple";
    public static final String JNT_RADIOBUTTON_FIELD = "jnt:radiobuttonField";
    public static final String JNT_PASSWORD_FIELD = "jnt:passwordField";
    public static final String JNT_CHECKBOX_FIELD = "jnt:checkboxField";
    public static final String JNT_SUBMIT_BUTTON = "jnt:submitButton";
    public static final String JNT_SELECT_FIELD = "jnt:selectField";
    public static final String JNT_SIMPLE_SEARCH_FORM = "jnt:simpleSearchForm";
    public static final String JNT_ADVANCED_SEARCH_FORM = "jnt:advancedSearchForm";
    public static final String JNT_SEARCH_RESULTS = "jnt:searchResults";

    private static ContentModelIconProvider iconProvider = new ContentModelIconProvider();

    private ContentModelIconProvider() {
    	super();
    }


    public static ContentModelIconProvider getInstance() {
        if (iconProvider == null) {
            iconProvider = new ContentModelIconProvider();
        }
        return iconProvider;
    }


    /**
     * Return an AbstractImagePrototype depending on the extension and the displayLock flag
     *
     * @param gwtJahiaNode
     * @return
     */
    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode) {
        if (gwtJahiaNode != null) {
            String ext = gwtJahiaNode.getExt();
            String type = null;
            if (gwtJahiaNode.getNodeTypes() != null && !gwtJahiaNode.getNodeTypes().isEmpty()) {
                type = gwtJahiaNode.getNodeTypes().get(0);
            }
            boolean isFolder = type != null && type.equalsIgnoreCase(JNT_FOLDER);
            boolean isOpened = gwtJahiaNode.isExpandOnLoad();
            return getIcon(type, ext,isFolder,isOpened);
        }
        return CONTENT_ICONS.file();
    }

    public AbstractImagePrototype getIcon(GWTJahiaNodeType gwtJahiaNodeType) {
        if (gwtJahiaNodeType != null) {
            String ext = gwtJahiaNodeType.getIcon();
            String typeName = gwtJahiaNodeType.getName();
            return getIcon(typeName, ext,false,false);
        }
        return CONTENT_ICONS.defaultNode();
    }

    private AbstractImagePrototype getIcon(String type, String ext,boolean isFolder,boolean isOpened) {        
        if(isFolder) {
            if(isOpened){
               return CONTENT_ICONS.folderOpen();
            }
             return CONTENT_ICONS.folderClose();
        }
        if (type != null) {
            if (ext.equalsIgnoreCase(DEFAULT_NODE)) {
                return CONTENT_ICONS.defaultNode();
            } else if (type.equalsIgnoreCase(FOLDER_CLOSE)) {
                return CONTENT_ICONS.folderClose();
            } else if (type.equalsIgnoreCase(FOLDER_OPEN)) {
                return CONTENT_ICONS.folderOpen();
            } else if (type.equalsIgnoreCase(JNT_ADDRESS)) {
                return CONTENT_ICONS.jntAddress();
            }
            // node type that begins with jahiaForum:
            else if (type.indexOf(JAHIA_FORUM) == 0) {
                return CONTENT_ICONS.jntForum();
            } else if (type.equalsIgnoreCase(JNT_RICHTEXT)) {
                return CONTENT_ICONS.jntRichText();
            } else if (type.equalsIgnoreCase(JNT_VIDEO)) {
                return CONTENT_ICONS.jntVideo();
            } else if (type.equalsIgnoreCase(JNT_TEXT)) {
                return CONTENT_ICONS.jntText();
            } else if (type.equalsIgnoreCase(JNT_FORM) || type.equals(JNT_SIMPLE_SEARCH_FORM) || type.equals(JNT_ADVANCED_SEARCH_FORM)) {
                return CONTENT_ICONS.jntForm();
            } else if (type.equalsIgnoreCase(JNT_IMAGE)) {
                return CONTENT_ICONS.jntImage();
            } else if (type.equalsIgnoreCase(JNT_MAIL)) {
                return CONTENT_ICONS.jntMail();
            } else if (type.equalsIgnoreCase(JNT_PUBLICATION)) {
                return CONTENT_ICONS.jntPublication();
            } else if (type.equalsIgnoreCase(JNT_TAG) || type.equalsIgnoreCase(JNT_PAGE_TAGGING) || type.equalsIgnoreCase(JNT_TAG_CLOUD)) {
                return CONTENT_ICONS.jntTag();
            } else if (type.equalsIgnoreCase(JNT_CATEGORY)) {
                return CONTENT_ICONS.jntCategory();
            } else if (type.equalsIgnoreCase(JNT_NEWS)) {
                return CONTENT_ICONS.jntNews();
            } else if (type.equalsIgnoreCase(JNT_PIECHART)) {
                return CONTENT_ICONS.jntPieChart();
            } else if (type.equalsIgnoreCase(JNT_FAQ)) {
                return CONTENT_ICONS.jntFaq();
            } else if (type.equalsIgnoreCase(JNT_BOOKMARK)) {
                return CONTENT_ICONS.jntBookmark();
            } else if (type.equalsIgnoreCase(JNT_SITE)) {
                return CONTENT_ICONS.jntSite();
            } else if (type.equalsIgnoreCase(JNT_INTERVIEW)) {
                return CONTENT_ICONS.jntInterview();
            } else if (type.equalsIgnoreCase(JNT_COMMENT)) {
                return CONTENT_ICONS.jntComment();
            } else if (type.equalsIgnoreCase(JNT_BLOGPOST)) {
                return CONTENT_ICONS.jntBlogpost();
            } else if (type.equalsIgnoreCase(JNT_EVENT)) {
                return CONTENT_ICONS.jntEvent();
            } else if (type.equalsIgnoreCase(JNT_PEOPLE)) {
                return CONTENT_ICONS.jntPeople();
            } else if (type.equalsIgnoreCase(PERCENT)) {
                return CONTENT_ICONS.percent();
            } else if (type.equalsIgnoreCase(JNT_FIELDSET)) {
                return CONTENT_ICONS.jntFieldset();
            } else if (type.equalsIgnoreCase(JNT_INPUT_TEXT)) {
                return CONTENT_ICONS.jntInputText();
            } else if (type.equalsIgnoreCase(JNT_RADIOBUTTON_FIELD)) {
                return CONTENT_ICONS.jntRadiobuttonField();
            } else if (type.equalsIgnoreCase(JNT_PASSWORD_FIELD)) {
                return CONTENT_ICONS.jntPasswordField();
            } else if (type.equalsIgnoreCase(JNT_CHECKBOX_FIELD)) {
                return CONTENT_ICONS.jntCheckboxField();
            } else if (type.equalsIgnoreCase(JNT_SUBMIT_BUTTON)) {
                return CONTENT_ICONS.jntButton();
            } else if (type.equalsIgnoreCase(JNT_SELECT_FIELD)) {
                return CONTENT_ICONS.jntSelectField();
            } else if (type.equalsIgnoreCase(JNT_INPUT_MULTIPLE)) {
                return CONTENT_ICONS.jntInputText();
            } else if (type.equals(JNT_SEARCH_RESULTS)) {
                return CONTENT_ICONS.list();
            }
        }
        if (ext != null) {
            if (ext.equalsIgnoreCase(CONTENT)) {
                return CONTENT_ICONS.content();
            } else if (ext.equalsIgnoreCase(DIR)) {
                return CONTENT_ICONS.dir();
            } else if (ext.equalsIgnoreCase(DOC)) {
                return CONTENT_ICONS.doc();
            } else if (ext.equalsIgnoreCase(EXE)) {
                return CONTENT_ICONS.exe();
            } else if (ext.equalsIgnoreCase(FILE)) {
                return CONTENT_ICONS.file();
            } else if (ext.equalsIgnoreCase(GEAR)) {
                return CONTENT_ICONS.gearth();
            } else if (ext.equalsIgnoreCase(HTML)) {
                return CONTENT_ICONS.html();
            } else if (ext.equalsIgnoreCase(IMG)) {
                return CONTENT_ICONS.img();
            } else if (ext.equalsIgnoreCase(LIST)) {
                return CONTENT_ICONS.list();
            } else if (ext.equalsIgnoreCase(MASHUP)) {
                return CONTENT_ICONS.mashup();
            } else if (ext.equalsIgnoreCase(PAGE)) {
                return CONTENT_ICONS.page();
            } else if (ext.equalsIgnoreCase(PDF)) {
                return CONTENT_ICONS.pdf();
            } else if (ext.equalsIgnoreCase(PLACE_HOLDER)) {
                return CONTENT_ICONS.placeholder();
            } else if (ext.equalsIgnoreCase(PORTLET)) {
                return CONTENT_ICONS.portlet();
            } else if (ext.equalsIgnoreCase(PPT)) {
                return CONTENT_ICONS.ppt();
            } else if (ext.equalsIgnoreCase(RAR)) {
                return CONTENT_ICONS.rar();
            } else if (ext.equalsIgnoreCase(SOUND)) {
                return CONTENT_ICONS.sound();
            } else if (ext.equalsIgnoreCase(TXT)) {
                return CONTENT_ICONS.txt();
            } else if (ext.equalsIgnoreCase(USER_GROUP)) {
                return CONTENT_ICONS.userGroup();
            } else if (ext.equalsIgnoreCase(USER)) {
                return CONTENT_ICONS.user();
            } else if (ext.equalsIgnoreCase(VIDEO)) {
                return CONTENT_ICONS.video();
            } else if (ext.equalsIgnoreCase(XLS)) {
                return CONTENT_ICONS.xls();
            } else if (ext.equalsIgnoreCase(ZIP)) {
                return CONTENT_ICONS.zip();
            } else if (ext.equalsIgnoreCase(LOCK)) {
                return CONTENT_ICONS.lock();
            } else if (ext.equalsIgnoreCase(QUERY) || ext.equalsIgnoreCase(SEARCH)) {
                return CONTENT_ICONS.query();
            } else if (ext.equalsIgnoreCase(INTERACTIVE)) {
                return CONTENT_ICONS.interactive();
            } else if (ext.equalsIgnoreCase(STRUCTURED)) {
                return CONTENT_ICONS.structured();
            } else if (ext.equalsIgnoreCase(CONTENTLIST)) {
                return CONTENT_ICONS.contentlist();
            }   else if (ext.equalsIgnoreCase(FORMCONTENT)) {
                return CONTENT_ICONS.formcontent();
            }   else if (ext.equalsIgnoreCase(SANDBOX)) {
                return CONTENT_ICONS.sandbox();
            }   else if (ext.equalsIgnoreCase(TEMPLATE)) {
                return CONTENT_ICONS.template();
            }
        }
        return CONTENT_ICONS.file();
    }

    public AbstractImagePrototype getLockIcon() {
        return CONTENT_ICONS.lock();
    }

    public AbstractImagePrototype getMinusRound() {
        return CONTENT_ICONS.minusRound();
    }

    public AbstractImagePrototype getPlusRound() {
        return CONTENT_ICONS.plusRound();
    }

    public AbstractImagePrototype getFolderCloseIcon() {
        return CONTENT_ICONS.folderClose();
    }

    public AbstractImagePrototype getFolderOpenIcon() {
        return CONTENT_ICONS.folderOpen();
    }
}
