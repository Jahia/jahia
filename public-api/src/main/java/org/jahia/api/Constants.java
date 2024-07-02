/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Commmon Jahia constants.
 * User: toto
 * Date: 6 juil. 2007
 * Time: 18:54:16
 */
public class Constants {
    public static final String JCR_ACTIVITY = "jcr:activity";
    public static final String JCR_AUTOCREATED = "jcr:autoCreated";
    public static final String JCR_BASEVERSION = "jcr:baseVersion";
    public static final String JCR_CHILD = "jcr:child";
    public static final String JCR_CHILDNODEDEFINITION = "jcr:childNodeDefinition";
    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_CREATEDBY = "jcr:createdBy";
    public static final String JCR_DATA = "jcr:data";
    public static final String JCR_DEFAULTPRIMARYTYPE = "jcr:defaultPrimaryType";
    public static final String JCR_DEFAULTVALUES = "jcr:defaultValues";
    public static final String JCR_DESCRIPTION = "jcr:description";
    public static final String JCR_ENCODING = "jcr:encoding";
    public static final String JCR_FROZENMIXINTYPES = "jcr:frozenMixinTypes";
    public static final String JCR_FROZENNODE = "jcr:frozenNode";
    public static final String JCR_FROZENPRIMARYTYPE = "jcr:frozenPrimaryType";
    public static final String JCR_FROZENUUID = "jcr:frozenUuid";
    public static final String JCR_HASORDERABLECHILDNODES = "jcr:hasOrderableChildNodes";
    public static final String JCR_ISCHECKEDOUT = "jcr:isCheckedOut";
    public static final String JCR_ISMIXIN = "jcr:isMixin";
    public static final String JCR_KEYWORDS = "jcr:keywords";
    public static final String JCR_LANGUAGE = "jcr:language";
    public static final String JCR_LASTMODIFIED = "jcr:lastModified";
    public static final String JCR_LASTMODIFIEDBY = "jcr:lastModifiedBy";
    public static final String JCR_LOCKISDEEP = "jcr:lockIsDeep";
    public static final String JCR_LOCKOWNER = "jcr:lockOwner";
    public static final String JCR_MANDATORY = "jcr:mandatory";
    public static final String JCR_MERGEFAILED = "jcr:mergeFailed";
    public static final String JCR_MIMETYPE = "jcr:mimeType";
    public static final String JCR_MIXINTYPES = "jcr:mixinTypes";
    public static final String JCR_MULTIPLE = "jcr:multiple";
    public static final String JCR_NAME = "jcr:name";
    public static final String JCR_NODETYPENAME = "jcr:nodeTypeName";
    public static final String JCR_ONPARENTVERSION = "jcr:onParentVersion";
    public static final String JCR_PREDECESSORS = "jcr:predecessors";
    public static final String JCR_PRIMARYITEMNAME = "jcr:primaryItemName";
    public static final String JCR_PRIMARYTYPE = "jcr:primaryType";
    public static final String JCR_PROPERTYDEFINITION = "jcr:propertyDefinition";
    public static final String JCR_PROTECTED = "jcr:protected";
    public static final String JCR_REQUIREDPRIMARYTYPES = "jcr:requiredPrimaryTypes";
    public static final String JCR_REQUIREDTYPE = "jcr:requiredType";
    public static final String JCR_ROOTVERSION = "jcr:rootVersion";
    public static final String JCR_SAMENAMESIBLINGS = "jcr:sameNameSiblings";
    public static final String JCR_STATEMENT = "jcr:statement";
    public static final String JCR_SUCCESSORS = "jcr:successors";
    public static final String JCR_SUPERTYPES = "jcr:supertypes";
    public static final String JCR_SYSTEM = "jcr:system";
    public static final String JCR_TITLE = "jcr:title";
    public static final String JCR_UUID = "jcr:uuid";
    public static final String JCR_VALUECONSTRAINTS = "jcr:valueConstraints";
    public static final String JCR_VERSIONHISTORY = "jcr:versionHistory";
    public static final String JCR_VERSIONLABELS = "jcr:versionLabels";
    public static final String JCR_VERSIONSTORAGE = "jcr:versionStorage";
    public static final String JCR_VERSIONABLEUUID = "jcr:versionableUuid";
    public static final String JCR_PATH = "jcr:path";
    public static final String JCR_SCORE = "jcr:score";
    public static final String MIX_LANGUAGE = "mix:language";
    public static final String MIX_LOCKABLE = "mix:lockable";
    public static final String MIX_MIMETYPE = "mix:mimeType";
    public static final String MIX_REFERENCEABLE = "mix:referenceable";
    public static final String MIX_VERSIONABLE = "mix:versionable";
    public static final String MIX_SIMPLEVERSIONABLE = "mix:simpleVersionable";
    public static final String MIX_CREATED = "mix:created";
    public static final String MIX_CREATED_BY = "mix:createdBy";
    public static final String MIX_LAST_MODIFIED = "mix:lastModified";
    public static final String MIX_TITLE = "mix:title";
    public static final String NT_BASE = "nt:base";
    public static final String NT_CHILDNODEDEFINITION = "nt:childNodeDefinition";
    public static final String NT_FILE = "nt:file";
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_FROZENNODE = "nt:frozenNode";
    public static final String NT_HIERARCHYNODE = "nt:hierarchyNode";
    public static final String NT_LINKEDFILE = "nt:linkedFile";
    public static final String NT_NODETYPE = "nt:nodeType";
    public static final String NT_PROPERTYDEFINITION = "nt:propertyDefinition";
    public static final String NT_QUERY = "nt:query";
    public static final String NT_RESOURCE = "nt:resource";
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    public static final String NT_VERSION = "nt:version";
    public static final String NT_VERSIONHISTORY = "nt:versionHistory";
    public static final String NT_VERSIONLABELS = "nt:versionLabels";
    public static final String NT_VERSIONEDCHILD = "nt:versionedChild";

    public static final String JAHIA_NS = "http://www.jahia.org/jahia/1.0";
    public static final String JAHIANT_NS = "http://www.jahia.org/jahia/nt/1.0";
    public static final String JAHIAMIX_NS = "http://www.jahia.org/jahia/mix/1.0";

    public static final String JAHIA_PREF = "j";
    public static final String JAHIANT_PREF = "jnt";
    public static final String JAHIAMIX_PREF = "jmix";

    public static final String JAHIAMIX_AUTOSPLITFOLDERS = "jmix:autoSplitFolders";
    public static final String JAHIAMIX_CATEGORIZED = "jmix:categorized";
    public static final String JAHIAMIX_LASTPUBLISHED = "jmix:lastPublished";
    public static final String JAHIAMIX_NODENAMEINFO = "jmix:nodenameInfo";
    public static final String JAHIAMIX_SHAREABLE = "jmix:shareable";
    public static final String JAHIAMIX_PUBLICATION = "jmix:publication";

    public static final String JAHIAMIX_BOUND_COMPONENT = "jmix:bindedComponent";
    public static final String JAHIAMIX_LIST = "jmix:list";

    public final static String JAHIAMIX_IMAGE = "jmix:image";

    public static final String JAHIAMIX_REFERENCES_IN_FIELD = "jmix:referencesInField";
    public static final String JAHIA_REFERENCE_IN_FIELD_PREFIX = "j:referenceInField*";

    public static final String JAHIAMIX_TAGGED = "jmix:tagged";

    public static final String JAHIAMIX_EXTERNALREFERENCE = "jmix:externalReference";

    public static final String JAHIANT_VIRTUALSITES_FOLDER = "jnt:virtualsitesFolder";

    public static final String JAHIAMIX_HIDDEN_NODE = "jmix:hiddenNode";

    public final static String JAHIAMIX_RB_TITLE = "jmix:rbTitle";

    public static final String JAHIAMIX_SEARCHABLE = "jmix:searchable";

    public static final String JAHIANT_FOLDER = "jnt:folder";
    public static final String JAHIANT_FILE = "jnt:file";
    public static final String JAHIANT_TEMP_FOLDER = "jnt:tempFolder";
    public static final String JAHIANT_TEMP_FILE = "jnt:tempFile";
    public static final String JAHIANT_RESOURCE = "jnt:resource";
    public static final String JAHIANT_SYMLINK = "jnt:symLink";
    public static final String JAHIAMIX_SYSTEMNODE = "jmix:systemNode";
    public static final String JAHIANT_VIRTUALSITE = "jnt:virtualsite";
    public static final String JAHIANT_SYSTEM = "jnt:system";
    public static final String JAHIANT_AXISFOLDER = "jnt:axisFolder";
    public static final String JAHIANT_USER = "jnt:user";
    public static final String JAHIANT_CATEGORY = "jnt:category";
    public static final String JAHIANT_TRANSLATION = "jnt:translation";
    public static final String JAHIANT_ACL = "jnt:acl";
    public static final String JAHIANT_ACE = "jnt:ace";
    public static final String JAHIANT_REFERENCEINFIELD = "jnt:referenceInField";

    public static final String JAHIANT_JAHIACONTENT = "jnt:jahiacontent";
    public static final String JAHIANT_PAGE = "jnt:page";
    public static final String JAHIANT_CONTENT = "jnt:content";
    public static final String JAHIANT_CONTENTLIST = "jnt:contentList";
    public static final String JAHIANT_PAGE_LINK = "jmix:link";
    public static final String JAHIANT_NODE_LINK = "jnt:nodeLink";
    public static final String JAHIANT_EXTERNAL_PAGE_LINK = "jnt:externalLink";
    public static final String JAHIANT_PORTLET = "jnt:portlet";
    public static final String JAHIANT_LAYOUT = "jnt:layout";
    public static final String JAHIANT_LAYOUTITEM = "jnt:layoutItem";
    public static final String JAHIANT_MOUNTPOINT = "jnt:mountPoint";
    public static final String JAHIANT_TAG = "jnt:tag";
    public static final String JAHIANT_TASK = "jnt:task";
    public static final String JAHIANT_TASKS = "jnt:tasks";
    public static final String JAHIANT_MAINRESOURCE_DISPLAY = "jnt:mainResourceDisplay";
    public static final String JAHIANT_AREA = "jnt:area";
    public static final String JAHIANT_QUERY = "jnt:query";

    public static final String JAHIANT_MODULEVERSIONFOLDER = "jnt:moduleVersionFolder";
    public static final String JAHIANT_NODETYPEFOLDER = "jnt:nodeTypeFolder";
    public static final String JAHIANT_TEMPLATETYPEFOLDER = "jnt:templateTypeFolder";
    public static final String JAHIANT_CSSFOLDER = "jnt:cssFolder";
    public static final String JAHIANT_CSSFILE = "jnt:cssFile";
    public static final String JAHIANT_JAVASCRIPTFOLDER = "jnt:javascriptFolder";
    public static final String JAHIANT_JAVASCRIPTFILE = "jnt:javascriptFile";
    public static final String JAHIANT_VIEWFILE = "jnt:viewFile";
    public static final String JAHIANT_TEMPLATEFILE = "jnt:templateFile";
    public static final String JAHIANT_TEMPLATESFOLDER = "jnt:templatesFolder";
    public static final String JAHIAMIX_VIEWPROPERTIES = "jmix:viewProperties";
    public static final String JAHIANT_DEFINITIONFILE = "jnt:definitionFile";
    public static final String JAHIANT_RESOURCEBUNDLE_FILE = "jnt:resourceBundleFile";
    public static final String JAHIANT_RESOURCEBUNDLE_FOLDER = "jnt:resourceBundleFolder";

    public static final String LASTPUBLISHED = "j:lastPublished";
    public static final String LASTPUBLISHEDBY = "j:lastPublishedBy";
    public static final String PUBLISHED = "j:published";
    public static final String WORKINPROGRESS = "j:workInProgress";
    public static final String WORKINPROGRESS_STATUS = "j:workInProgressStatus";
    public static final String WORKINPROGRESS_LANGUAGES = "j:workInProgressLanguages";
    public static final String WORKINPROGRESS_STATUS_ALLCONTENT = "ALL_CONTENT";
    public static final String WORKINPROGRESS_STATUS_LANG = "LANGUAGES";
    public static final String WORKINPROGRESS_STATUS_DISABLED = "DISABLED";

    public static final String CONTENT = "content";
    public static final String SYSTEM = "j:system";
    public static final String FILTERS = "j:filters";
    public static final String AUTOMATIONS = "j:automations";
    public static final String EXTRACTED_TEXT = "j:extractedText";
    public static final String EXTRACTION_DATE = "j:lastExtractionDate";
    public static final String ORIGINAL_UUID = "j:originalUuid";
    public static final String NODENAME = "j:nodename";
    public static final String PROCESSID = "j:processId";
    public static final String URL = "j:url";
    public static final String NODE = "j:node";
    public static final String ALT = "j:alt";
    public static final String TARGET = "j:target";
    public static final String AREA_NAME = "j:areaName";
    public static final String SITETYPE = "j:moduleType";

    public static final String ACL = "j:acl";
    public static final String APPLY_ACL = "j:applyAcl";
    public static final String THUMBNAIL = "j:thumbnail";
    // j:fullpath is deprecated
    public static final String FULLPATH = "j:fullpath";
    public static final String TAGS = "j:tags";
    public static final String TAG_LIST = "j:tagList";
    public static final String KEYWORDS = "j:keywords";
    public static final String DEFAULT_CATEGORY = "j:defaultCategory";
    public static final String DESCRIPTION = "j:description";
    public static final String TITLE = "j:title";

    public static final String SPLIT_CONFIG = "j:splitConfig";
    public static final String SPLIT_NODETYPE = "j:splitNodeType";

    public static final String JAHIANT_CONDITION = "jnt:condition";
    public static final String JAHIANT_CONDITIONAL_VISIBILITY = "jnt:conditionalVisibility";

    public static final String JAHIANT_GROUP = "jnt:group";
    public static final String JAHIANT_MEMBERS = "jnt:members";
    public static final String JAHIANT_MEMBER = "jnt:member";

    public static final String JCR_READ_RIGHTS = "jcr:read";
    public static final String JCR_ALL_RIGHTS = "jcr:all";
    public static final String JCR_WRITE_RIGHTS = "jcr:write";
    public static final String JCR_MODIFYACCESSCONTROL_RIGHTS = "jcr:modifyAccessControl";

    public static final String JCR_READ_RIGHTS_LIVE = "jcr:read_live";
    public static final String JCR_WRITE_RIGHTS_LIVE = "jcr:write_live";
    public static final String JCR_ADD_CHILD_NODES_LIVE = "jcr:addChildNodes_live";

    public static final String GRANT = "GRANT";
    public static final String DENY = "DENY";

    public static final String LIVE_WORKSPACE = "live";
    public static final String EDIT_WORKSPACE = "default";

    public static final String GUEST_USERNAME = "guest";

    public static final String SESSION_LOCALE = "org.jahia.services.multilang.currentlocale";
    public static final String SESSION_UI_LOCALE = "org.jahia.services.multilang.uilocale";
    public static final String SESSION_USER = "org.jahia.usermanager.jahiauser";

    public static final String REVISION_NUMBER = "j:revisionNumber";
    public static final String CHECKIN_DATE = "j:checkinDate";

    public static final String ORIGIN_WORKSPACE = "j:originWS";

    public static final String JAHIAMIX_WORKFLOW = "jmix:workflow";

    public static final String JAHIA_SOURCE_TEMPLATE = "j:sourceTemplate";
    public static final String JAHIA_MODULE_TEMPLATE = "j:moduleTemplate";

    public static final String JAHIAMIX_MARKED_FOR_DELETION = "jmix:markedForDeletion";
    public static final String JAHIAMIX_MARKED_FOR_DELETION_ROOT = "jmix:markedForDeletionRoot";
    public static final String MARKED_FOR_DELETION_LOCK_TYPE = "deletion";
    public static final String MARKED_FOR_DELETION_LOCK_USER = " deletion ";
    public static final String MARKED_FOR_DELETION_USER = "j:deletionUser";
    public static final String MARKED_FOR_DELETION_DATE = "j:deletionDate";
    public static final String MARKED_FOR_DELETION_MESSAGE = "j:deletionMessage";

    public static final String JAHIA_TITLE_KEY = "j:titleKey";

    public static final String JAHIA_EDITABLE_IN_CONTRIBUTION = "j:editableInContribution";
    public static final String JAHIA_CONTRIBUTE_TYPES = "j:contributeTypes";

    public static final String JAHIAMIX_NAVMENUITEM = "jmix:navMenuItem";

    public static final Set<String> forbiddenPropertiesToCopy = new HashSet<>(Arrays.asList(PROCESSID, JCR_FROZENUUID,
            JCR_FROZENMIXINTYPES, JCR_FROZENPRIMARYTYPE, JCR_UUID, JCR_BASEVERSION, JCR_MIXINTYPES, JCR_PRIMARYTYPE,
            JCR_VERSIONHISTORY, JCR_PREDECESSORS, JCR_ISCHECKEDOUT, JCR_CREATED, JCR_CREATEDBY, "j:lockTypes",
            "j:locktoken", JCR_LOCKOWNER, JCR_LOCKISDEEP, JAHIA_SOURCE_TEMPLATE, PUBLISHED, MARKED_FOR_DELETION_DATE,
            MARKED_FOR_DELETION_MESSAGE, MARKED_FOR_DELETION_USER, ORIGIN_WORKSPACE, LASTPUBLISHED, LASTPUBLISHEDBY));

    public static final Set<String> forbiddenMixinToCopy = new HashSet<>(Arrays.asList(JAHIAMIX_WORKFLOW,
            JAHIAMIX_MARKED_FOR_DELETION, JAHIAMIX_MARKED_FOR_DELETION_ROOT, "jmix:vanityUrlMapped", "jmix:hideDeleteAction"));

    public static final Set<String> forbiddenChildNodeTypesToCopy = new HashSet<>(Arrays.asList(JAHIAMIX_MARKED_FOR_DELETION_ROOT,
            JAHIANT_REFERENCEINFIELD, "jnt:vanityUrls"));

    public static final String JCR_LASTLOGINDATE = "lastLoginDate";
    public static final String J_ROLES = "j:roles";
    public static final String JAHIANT_PERMISSION = "jnt:permission";
    public static final String JAHIANT_ROLE = "jnt:role";
    public static final String JAHIANT_ROLES = "jnt:roles";
    public static final String J_PERMISSIONNAMES = "j:permissionNames";

    public static final Set<String> forbiddenPropertiesToSerialize = new HashSet<String>(Arrays.asList(PROCESSID, JCR_FROZENUUID,
            JCR_FROZENMIXINTYPES, JCR_FROZENPRIMARYTYPE, JCR_BASEVERSION,
            JCR_VERSIONHISTORY, JCR_PREDECESSORS, JCR_ISCHECKEDOUT));

    public static final String JAHIA_PROJECT_VERSION;
    public static final String SCM_DUMMY_URI = "scm:dummy:uri";

    public static final String UI_THEME = "jahia.ui.theme";

    public static final String TO_CACHE_WITH_PARENT_FRAGMENT = "toCacheWithParentFragment";

    public static final String JAHIAMIX_AUTO_PUBLISH = "jmix:autoPublish";
    public static final String JAHIAMIX_MAIN_RESOURCE = "jmix:mainResource";
    public static final String JAHIAMIX_NOLIVE = "jmix:nolive";

    public static final String JAHIAMIX_I18N = "jmix:i18n";
    public static final String INVALID_LANGUAGES = "j:invalidLanguages";

    public static final String JAHIAMIX_LIVE_PROPERTIES = "jmix:liveProperties";
    public static final String LIVE_PROPERTIES = "j:liveProperties";

    public static final String JAHIANT_CONTENT_FOLDER = "jnt:contentFolder";
    public static final String JAHIANT_CONTENT_TEMPLATE = "jnt:contentTemplate";
    public static final String JAHIANT_PAGE_TEMPLATE = "jnt:pageTemplate";

    /**
     * The set of properties that are <strong>NOT</strong> internationalized properties but are still copied to translation nodes.
     * <p>
     * TODO: this set might not be exhaustive at the moment. Please complete if needed!
     */
    public static final Set<String> nonI18nPropertiesCopiedToTranslationNodes = new HashSet<>(Arrays.asList(PUBLISHED, LASTPUBLISHED, LASTPUBLISHEDBY));

    public static final String CLUSTER_BROADCAST_TOPIC_PREFIX = "org/jahia/cluster/broadcast";

    static {
        Properties p = new Properties();
        try {
            p.load(Constants.class.getClassLoader().getResourceAsStream("version.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JAHIA_PROJECT_VERSION = p.getProperty("version");
    }
}
