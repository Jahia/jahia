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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.iconsLarge.ContentIconsLargeImageBundle;

/**
 * Icon provider for for different types of content objects.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ContentModelIconProvider implements ModelIconProvider<GWTJahiaNode> {

    public static final ContentIconsImageBundle CONTENT_ICONS = GWT.create(ContentIconsImageBundle.class);
    public static final ContentIconsImageBundle CONTENT_ICONS_LARGE = GWT.create(ContentIconsLargeImageBundle.class);

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
            return getIcon(type, ext, isFolder, isOpened, false);
        }
        return getContentIconsBundle(false).file();
    }

    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode, boolean large) {
        if (gwtJahiaNode != null) {
            String ext = gwtJahiaNode.getExt();
            String type = null;
            if (gwtJahiaNode.getNodeTypes() != null && !gwtJahiaNode.getNodeTypes().isEmpty()) {
                type = gwtJahiaNode.getNodeTypes().get(0);
            }
            boolean isFolder = type != null && type.equalsIgnoreCase(JNT_FOLDER);
            boolean isOpened = gwtJahiaNode.isExpandOnLoad();
            return getIcon(type, ext, isFolder, isOpened, large);
        }
        return getContentIconsBundle(large).file();
    }

    public AbstractImagePrototype getIcon(GWTJahiaNodeType gwtJahiaNodeType) {
        if (gwtJahiaNodeType != null) {
            String ext = gwtJahiaNodeType.getIcon();
            String typeName = gwtJahiaNodeType.getName();
            return getIcon(typeName, ext, false, false, false);
        }
        return getContentIconsBundle(false).defaultNode();
    }

    public AbstractImagePrototype getIcon(GWTJahiaNodeType gwtJahiaNodeType, boolean large) {
        if (gwtJahiaNodeType != null) {
            String ext = gwtJahiaNodeType.getIcon();
            String typeName = gwtJahiaNodeType.getName();
            return getIcon(typeName, ext, false, false, large);
        }
        return getContentIconsBundle(large).defaultNode();
    }

    private AbstractImagePrototype getIcon(String type, String ext, boolean isFolder, boolean isOpened, boolean large) {

        ContentIconsImageBundle ci = getContentIconsBundle(large);
        if (isFolder) {
            if (isOpened) {
                return ci.folderOpen();
            }
            return ci.folderClose();
        }
        if (type != null) {
            if (ext.equalsIgnoreCase(DEFAULT_NODE)) {
                return ci.defaultNode();
            } else if (type.equalsIgnoreCase(FOLDER_CLOSE)) {
                return ci.folderClose();
            } else if (type.equalsIgnoreCase(FOLDER_OPEN)) {
                return ci.folderOpen();
            } else if (type.equalsIgnoreCase(JNT_ADDRESS)) {
                return ci.jntAddress();
            }
            // node type that begins with jahiaForum:
            else if (type.indexOf(JAHIA_FORUM) == 0) {
                return ci.jntForum();
            } else if (type.equalsIgnoreCase(JNT_RICHTEXT)) {
                return ci.jntRichText();
            } else if (type.equalsIgnoreCase(JNT_VIDEO)) {
                return ci.jntVideo();
            } else if (type.equalsIgnoreCase(JNT_TEXT)) {
                return ci.jntText();
            } else if (type.equalsIgnoreCase(JNT_FORM) || type.equals(JNT_SIMPLE_SEARCH_FORM) || type.equals(JNT_ADVANCED_SEARCH_FORM)) {
                return ci.jntForm();
            } else if (type.equalsIgnoreCase(JNT_IMAGE)) {
                return ci.jntImage();
            } else if (type.equalsIgnoreCase(JNT_MAIL)) {
                return ci.jntMail();
            } else if (type.equalsIgnoreCase(JNT_PUBLICATION)) {
                return ci.jntPublication();
            } else if (type.equalsIgnoreCase(JNT_TAG) || type.equalsIgnoreCase(JNT_PAGE_TAGGING) || type.equalsIgnoreCase(JNT_TAG_CLOUD)) {
                return ci.jntTag();
            } else if (type.equalsIgnoreCase(JNT_CATEGORY)) {
                return ci.jntCategory();
            } else if (type.equalsIgnoreCase(JNT_NEWS)) {
                return ci.jntNews();
            } else if (type.equalsIgnoreCase(JNT_PIECHART)) {
                return ci.jntPieChart();
            } else if (type.equalsIgnoreCase(JNT_FAQ)) {
                return ci.jntFaq();
            } else if (type.equalsIgnoreCase(JNT_BOOKMARK)) {
                return ci.jntBookmark();
            } else if (type.equalsIgnoreCase(JNT_SITE)) {
                return ci.jntSite();
            } else if (type.equalsIgnoreCase(JNT_INTERVIEW)) {
                return ci.jntInterview();
            } else if (type.equalsIgnoreCase(JNT_COMMENT)) {
                return ci.jntComment();
            } else if (type.equalsIgnoreCase(JNT_BLOGPOST)) {
                return ci.jntBlogpost();
            } else if (type.equalsIgnoreCase(JNT_EVENT)) {
                return ci.jntEvent();
            } else if (type.equalsIgnoreCase(JNT_PEOPLE)) {
                return ci.jntPeople();
            } else if (type.equalsIgnoreCase(PERCENT)) {
                return ci.percent();
            } else if (type.equalsIgnoreCase(JNT_FIELDSET)) {
                return ci.jntFieldset();
            } else if (type.equalsIgnoreCase(JNT_INPUT_TEXT)) {
                return ci.jntInputText();
            } else if (type.equalsIgnoreCase(JNT_RADIOBUTTON_FIELD)) {
                return ci.jntRadiobuttonField();
            } else if (type.equalsIgnoreCase(JNT_PASSWORD_FIELD)) {
                return ci.jntPasswordField();
            } else if (type.equalsIgnoreCase(JNT_CHECKBOX_FIELD)) {
                return ci.jntCheckboxField();
            } else if (type.equalsIgnoreCase(JNT_SUBMIT_BUTTON)) {
                return ci.jntButton();
            } else if (type.equalsIgnoreCase(JNT_SELECT_FIELD)) {
                return ci.jntSelectField();
            } else if (type.equalsIgnoreCase(JNT_INPUT_MULTIPLE)) {
                return ci.jntInputText();
            } else if (type.equals(JNT_SEARCH_RESULTS)) {
                return ci.list();
            } else if (type.startsWith("jnt:navMenu")) {
                return ci.navMenu();
            }
        }
        if (ext != null) {
            if (ext.equalsIgnoreCase(CONTENT)) {
                return ci.content();
            } else if (ext.equalsIgnoreCase(DIR)) {
                return ci.dir();
            } else if (ext.equalsIgnoreCase(DOC)) {
                return ci.doc();
            } else if (ext.equalsIgnoreCase(EXE)) {
                return ci.exe();
            } else if (ext.equalsIgnoreCase(FILE)) {
                return ci.file();
            } else if (ext.equalsIgnoreCase(GEAR)) {
                return ci.gearth();
            } else if (ext.equalsIgnoreCase(HTML)) {
                return ci.html();
            } else if (ext.equalsIgnoreCase(IMG)) {
                return ci.img();
            } else if (ext.equalsIgnoreCase(LIST)) {
                return ci.list();
            } else if (ext.equalsIgnoreCase(MASHUP)) {
                return ci.mashup();
            } else if (ext.equalsIgnoreCase(PAGE)) {
                return ci.page();
            } else if (ext.equalsIgnoreCase(PDF)) {
                return ci.pdf();
            } else if (ext.equalsIgnoreCase(PLACE_HOLDER)) {
                return ci.placeholder();
            } else if (ext.equalsIgnoreCase(PORTLET)) {
                return ci.portlet();
            } else if (ext.equalsIgnoreCase(PPT)) {
                return ci.ppt();
            } else if (ext.equalsIgnoreCase(RAR)) {
                return ci.rar();
            } else if (ext.equalsIgnoreCase(SOUND)) {
                return ci.sound();
            } else if (ext.equalsIgnoreCase(TXT)) {
                return ci.txt();
            } else if (ext.equalsIgnoreCase(USER_GROUP)) {
                return ci.userGroup();
            } else if (ext.equalsIgnoreCase(USER)) {
                return ci.user();
            } else if (ext.equalsIgnoreCase(VIDEO)) {
                return ci.video();
            } else if (ext.equalsIgnoreCase(XLS)) {
                return ci.xls();
            } else if (ext.equalsIgnoreCase(ZIP)) {
                return ci.zip();
            } else if (ext.equalsIgnoreCase(LOCK)) {
                return ci.lock();
            } else if (ext.equalsIgnoreCase(QUERY) || ext.equalsIgnoreCase(SEARCH)) {
                return ci.query();
            } else if (ext.equalsIgnoreCase(INTERACTIVE)) {
                return ci.interactive();
            } else if (ext.equalsIgnoreCase(STRUCTURED)) {
                return ci.structured();
            } else if (ext.equalsIgnoreCase(CONTENTLIST)) {
                return ci.contentlist();
            } else if (ext.equalsIgnoreCase(FORMCONTENT)) {
                return ci.formcontent();
            } else if (ext.equalsIgnoreCase(SANDBOX)) {
                return ci.sandbox();
            } else if (ext.equalsIgnoreCase(TEMPLATE)) {
                return ci.template();
            }
        }
        return ci.file();
    }

    private ContentIconsImageBundle getContentIconsBundle(boolean large) {
        ContentIconsImageBundle ci = CONTENT_ICONS;
        if (large) {
            ci = CONTENT_ICONS_LARGE;
        }
        return ci;
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

    public AbstractImagePrototype getMoveFirst() {
        return CONTENT_ICONS.moveFirst();
    }

    public AbstractImagePrototype getMoveUp() {
        return CONTENT_ICONS.moveUp();
    }

    public AbstractImagePrototype getMoveDown() {
        return CONTENT_ICONS.moveDown();
    }

    public AbstractImagePrototype getMoveLast() {
        return CONTENT_ICONS.moveLast();
    }

    public AbstractImagePrototype getFolderCloseIcon() {
        return CONTENT_ICONS.folderClose();
    }

    public AbstractImagePrototype getFolderOpenIcon() {
        return CONTENT_ICONS.folderOpen();
    }
}
