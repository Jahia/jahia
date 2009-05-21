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
package org.jahia.services.importexport;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.StructuralRelationship;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentApplicationField;
import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.jcr.RepositoryException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 21, 2008
 * Time: 5:09:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Exporter {
    protected static final String GHOST = "GHOST";
    protected static final String CDATA = "CDATA";

    private static Logger logger = Logger
            .getLogger(Exporter.class);

    protected List<String> structRelations;
    protected List<String> pickerRelations;

    public Exporter() {
        structRelations = new ArrayList<String>();
        structRelations.add(StructuralRelationship.CONTENT_LINK);
        structRelations.add(StructuralRelationship.METADATA_LINK);
        structRelations.add(StructuralRelationship.METADATADEFINITION_LINK);
        structRelations.add("category");
        structRelations.add("reference");
        pickerRelations = new ArrayList<String>();
        pickerRelations.add(StructuralRelationship.ACTIVATION_PICKER_LINK);
        pickerRelations.add(StructuralRelationship.CHANGE_PICKER_LINK);
    }


    public abstract void export(ContentObject object, String languageCode, ContentHandler h, Set files, ProcessingContext jParams, Map params) throws JahiaException, SAXException;

    protected String getFieldValue(ContentField contentField, Set<JCRNodeWrapper> files, ProcessingContext jParams, ContentObjectEntryState entryState, AttributesImpl attr) throws JahiaException {
        if (contentField.getType() != ContentFieldTypes.APPLICATION) {
            String value = contentField.getValue(jParams, entryState);
            if ("<empty>".equals(value) || value == null) {
                return null;
            }
            switch (contentField.getType()) {
                case ContentFieldTypes.BIGTEXT:
                    return parseBigtextLinks(parseBigtextFiles(value, files, jParams));
                case ContentFieldTypes.DATE:
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        return df.format(new Date(Long.parseLong(value)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case ContentFieldTypes.FILE:
                    if (files != null) {
                        JCRNodeWrapper file = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(value, jParams.getUser());
                        if (file.isValid() && !files.contains(file)) {
                            files.add(file);
                            value = file.getPath();
                        } else {
                            value = "";
                        }
                    }
                    return value;
                case ContentFieldTypes.SMALLTEXT:
                case ContentFieldTypes.SMALLTEXT_SHARED_LANG:
                    if (value.startsWith("<jahia-expression") && value.endsWith("/>")) {
                        try {
                            Pattern p = Pattern.compile("(.*)getContainerByID\\(([0-9]+)\\)(.*)");
                            Matcher m = p.matcher(value);
                            while (m.matches()) {
                                String replacement = m.group(1) + "getContainerByUUID(\"" +
                                        ServicesRegistry.getInstance().getImportExportService().getUuid(ContentContainer.getChildInstance(m.group(2))) +
                                        "\")" + m.group(3);
                                m.reset();
                                value = m.replaceFirst(replacement);
                                m = p.matcher(value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    return value;
                default:
                    return value;
            }
        } else {
            String epInstanceID = (((ContentApplicationField) contentField).getAppID(entryState));
            ApplicationsManagerService applicationManagerService = ServicesRegistry.getInstance().getApplicationsManagerService();
            EntryPointInstance epi = applicationManagerService.getEntryPointInstance(epInstanceID);
            if (epi != null) {
                try {
                    return JCRStoreService.getInstance().getNodeByUUID(epi.getID(), jParams.getUser()).getPath();
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return null;
        }
    }

    public void getFiles(ContentObject object, String language, Set<JCRNodeWrapper> files, ProcessingContext jParams, Set included, EntryLoadRequest toLoadRequest) throws JahiaException {
        if (!object.checkReadAccess(jParams.getUser())) {
            return;
        }

        getFilesForField(object, jParams, language, toLoadRequest, files);

        // Recurse on children
        EntryLoadRequest withDeleted = new EntryLoadRequest(toLoadRequest);
        withDeleted.setWithMarkedForDeletion(true);
        List<? extends ContentObject> l = object.getChilds(jParams.getUser(), withDeleted);

        if (object instanceof ContentContainerList) {
            ImportExportUtils.orderContainerList(l, jParams);
        }

        for (ContentObject child : l) {
            if (included == null || included.contains(child.getObjectKey())) {
                getFiles(child, language, files, jParams, included, toLoadRequest);
            }
        }
    }

    public void getFilesForField(ContentObject object, ProcessingContext jParams, String language, EntryLoadRequest loadRequest, Set<JCRNodeWrapper> files) throws JahiaException {
        ContentObjectEntryState entryState = getEntryState(object, language, loadRequest);

        if (entryState == null) {
            return;
        }

        if (object instanceof ContentFileField) {
            ContentField contentField = (ContentField) object;
            String value = contentField.getValue(jParams, entryState);

            JCRNodeWrapper file = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(value, jParams.getUser());
            if (file.isValid() && !files.contains(file)) {
                logger.debug("Found file for "+object.getObjectKey() + " : " +file.getPath());
                files.add(file);
            }
        } else if (object instanceof ContentBigTextField) {
            String value = ((ContentBigTextField)object).getValue(jParams, entryState);
            Set<JCRNodeWrapper> f =  new HashSet<JCRNodeWrapper>();
            parseBigtextFiles(value, f, jParams);
            for (Iterator<JCRNodeWrapper> iterator = f.iterator(); iterator.hasNext();) {
                JCRNodeWrapper file = iterator.next();

                if (file.isValid() && !files.contains(file)) {
                    logger.debug("Found file for "+object.getObjectKey() + " : " +file.getPath());
                    files.add(file);
                }
            }
        }
    }

    public String parseBigtextFiles(String rawValue, Set<JCRNodeWrapper> files, ProcessingContext jParams) throws JahiaException {
        String str = "###/webdav";
        if (rawValue == null || rawValue.length() == 0 ||
                rawValue.toLowerCase().indexOf(str) < 0) {
            return rawValue;
        }

        JahiaSite site = jParams.getSite();

        StringBuffer b = new StringBuffer(rawValue);
        int i = -1;
        while ((i = b.indexOf(str, i+1)) > -1) {
            String boundary = Character.toString(b.charAt(i-1));
            if (boundary.equals(";")) {
                boundary = b.substring(b.lastIndexOf("&",i), i);
            }
            int from = i + str.length();
            int to = b.indexOf(boundary, from);
            String l = b.substring(from,to);
            StringTokenizer st = new StringTokenizer(l,"/");
            String fname = "";
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                fname += "/" + t;
                if ("site".equals(t)) {
                    site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(st.nextToken());
                    fname = "";
                }
            }
            b.replace(from,to,fname);
            try {
                JCRNodeWrapper f = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(URLDecoder.decode(fname), jParams.getUser());
                if (f.isValid()) {
                    files.add(f);
                }
            } catch (Exception e) {
                logger.warn("Invalid link to file "+l);
            }
        }
        return b.toString();
    }

    public String parseBigtextLinks(String rawValue) throws JahiaException {
        if (rawValue == null || rawValue.length() == 0 ||
                rawValue.toLowerCase().indexOf(" href") < 0) {
            return rawValue;
        }

        StringBuffer b = new StringBuffer(rawValue);
        int i = -1;
        while ((i = b.indexOf(" href=", i+1)) > -1) {
            int j = b.indexOf("/pid/", i);
            int l = b.indexOf(" ", i+2);
            if (j!=-1 && (l==-1 || j<l)) {
                StringBuffer pidb = new StringBuffer();
                int k = j+5;
                for (; i < b.length() && Character.isDigit(b.charAt(k)); k++) {
                    pidb.append(b.charAt(k));
                }
                try {
                    int pid = Integer.parseInt(pidb.toString());
                    try {
                        String uuid = ServicesRegistry.getInstance().getImportExportService().getUuid(ContentPage.getPage(pid));
                        b.replace(j,k,"/ref/"+uuid);
                    } catch (JahiaPageNotFoundException e) {
                        logger.warn("Invalid link to page "+pid);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid pid "+pidb);
                }
            }
        }
        return b.toString();
    }



    protected ContentObjectEntryState getEntryState(ContentObject object, String languageCode, EntryLoadRequest loadRequest) throws JahiaException {
        ContentObjectEntryState entryState = null;
        int vID = loadRequest.getVersionID();
        if (vID == 0 && loadRequest.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            vID = 1;
        }
        if (object.isShared()) {
            languageCode = ContentField.SHARED_LANGUAGE;
        }
        entryState =
                ContentObjectEntryState.getEntryState(vID,
                        languageCode);
        entryState = object.getEntryState(entryState, false, true);

        return entryState;
    }



    class Siblings {
        List elements = new ArrayList();
        boolean printall = false;
    }

    class Element {
        String uri;
        String elementName;
        String qName;
        AttributesImpl attr;
        boolean started = false;

        public Element(String uri, String elementName, String qName, AttributesImpl attr) {
            this.uri = uri;
            this.elementName = elementName.replace(' ' , '_');
            this.qName = qName;
            this.attr = attr;
        }
    }


}
