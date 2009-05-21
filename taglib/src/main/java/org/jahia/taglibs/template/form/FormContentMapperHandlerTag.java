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
package org.jahia.taglibs.template.form;

import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.captcha.CaptchaService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.*;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.tools.files.FileUpload;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>Title: Form handler that maps to Content objects </p>
 * <p>Description: This is a very powerful tag that allows to map a form
 * submission to a container to be added in a container list.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 * @jsp:tag name="formContentMapperHandler" body-content="empty"
 * description="Processes a submitted form and maps the parameters to fields in a container and adds the container
 * to the specified container list.
 * <p/>
 * <p><attriInfo>In the example below, the formContentMapperHandler tag defines the mapping between the submitToTestContainerList form and
 * its associated container list testContainerList. The attribute 'submitMarker'  specifies which form on the page to link to
 * and 'listName' the associated container list. Its value can be set to any &lt;input&gt; tag of the form with a
 * unique name (to the page) such as submitToTestContainerList in this case. If the 'submitMarker' attribute is undefined,
 * the form will not be processed.
 * <p/>
 * <p>Given the fields in the submitToTestContainerList form, the formContentMapperHandler tag expects to find the fields named
 * title, content and bookformat in the container list testContainerList  on the current page.
 * If this isn't the case, an error is generated.
 * <p/>
 * <p><b>Example :</b>
 * <p/>
 * ...<br>
 * &nbsp;&lt;content:declareContainerList name=\"testContainerList\" title=\"Content Container list\"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;content:declareContainer&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content:declareField name=\"title\" title=\"Title\" type=\"SmallText\" value=\"Lord of the Flies\"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content:declareField name=\"bookformat\" title=\"BookFormat\" type=\"SharedSmallText\" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content:declareField name=\"content\" title=\"Content\" type=\"BigText\" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;/content:declareContainer&gt;<br>
 * &nbsp;    &lt;/content:declareContainerList&gt;<br>
 * &nbsp;   &lt;%<br>
 * &nbsp;   ...<br>
 * &nbsp;   String bypassUrl = jParams.composePageUrl(jData.page().getID());<br>
 * &nbsp;   ...<br>
 * &nbsp;   %&gt;<br>
 * &nbsp;&lt;/head&gt;<br>
 * &nbsp;&lt;body&gt;<br>
 * ...<br>
 * <br>
 * &nbsp;&lt;h1&gt;Add container with form&lt;/h1&gt;<br>
 * <br>
 * &nbsp;&lt;content:containerList name=\"testContainerList\" id=\"testContainerList\"&gt;<br>
 * <br>
 * &nbsp;    &lt;content:container id=\"testContainer\"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;h2&gt;&lt;content:textField name=\"title\" defaultValue=\"\"/&gt;<br>
 * &nbsp;&lt;content:textField name=\"bookformat\" defaultValue=\"\"/&gt;&lt;/h2&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;content:bigTextField name=\"content\" defaultValue=\"\"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;br/&gt;<br>
 * &nbsp;    &lt;/content:container&gt;<br>
 * <br>
 * &nbsp;    &lt;hr/&gt;<br>
 * &nbsp;<br>
 * &nbsp;&lt;/content:containerList&gt;<br>
 * <br>
 * &nbsp;&lt;content:formContentMapperHandler listName=\"testContainerList\" submitMarker=\"submitToTestContainerList\" /&gt;<br>
 * &nbsp;&lt;form action=\"&lt;%=bypassUrl%&gt;\" name=\"submitToContainerList\" method=\"post\"&gt;<br>
 * &nbsp;    &lt;input type=\"text\" name=\"title\" value=\"Lord of the Flies\"/&gt;&lt;br/&gt;<br>
 * &nbsp;    &lt;input type=\"text\" name=\"bookformat\"/&gt;&lt;br/&gt;<br>
 * &lt;textarea name=\"content\"&gt;&lt;/textarea&gt;&lt;br/&gt;<br>
 * &lt;input type=\"submit\" name=\"submitToTestContainerList\" value=\"Submit\"/&gt;<br>
 * &nbsp;&lt;/form&gt;<br>
 * ...<br>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class FormContentMapperHandlerTag extends AbstractJahiaTag {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(FormContentMapperHandlerTag.class);

    private boolean immediatePublication = false;
    private String listName = null;
    private String storeAsUserName = JahiaUserManagerService.GUEST_USERNAME;
    private String storeAsGroupName = JahiaGroupManagerService.GUEST_GROUPNAME;
    private String submitMarker = null;
    private String captchaFieldName = null;

    /**
     * @jsp:attribute name="immediatePublication" required="false" rtexprvalue="true" type="Boolean"
     * description="This attribute specifies whether user-submitted containers should be immediately
     * validated or left in staging mode.
     * <p><attriInfo> The default value is 'false' i.e. it is saved in the staging mode
     * only.  The language is not specified so the content is automatically added in the browser's current
     * language. If necessary, you can get around this by declaring the fields as shared type in the
     * container (i.e. type=\"SharedSmallText\") so that the value will be independent of the browser's language.
     * </attriInfo>"
     */
    public boolean isImmediatePublication() {
        return immediatePublication;
    }

    public void setImmediatePublication(boolean immediatePublication) {
        this.immediatePublication = immediatePublication;
    }

    /**
     * @jsp:attribute name="listName" required="false" rtexprvalue="true"
     * description="the name of the container list where to create and store user-submitted containers.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    /**
     * @jsp:attribute name="storeAsUserName" required="false" rtexprvalue="true"
     * description="give ACL rights to stored containers to the specified user only.
     * <p/>
     * <p><attriInfo>By default, the ACL on the stored containers will give Read/Write and Admin rights to user Guest,
     * it is wise to restrict this level of access to only a single user.
     * </attriInfo>"
     */
    public String getStoreAsUserName() {
        return storeAsUserName;
    }

    public void setStoreAsUserName(String storeAsUserName) {
        this.storeAsUserName = storeAsUserName;
    }

    /**
     * @jsp:attribute name="storeAsGroupName" required="false" rtexprvalue="true" type="Boolean"
     * description="give ACL rights to stored containers to the specified user only.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getStoreAsGroupName() {
        return storeAsGroupName;
    }

    public void setStoreAsGroupName(String storeAsGroupName) {
        this.storeAsGroupName = storeAsGroupName;
    }

    /**
     * @jsp:attribute name="submitMarker" required="false" rtexprvalue="true" type="Boolean"
     * description="The attribute submitMarker  specifies which form on the page to link to.
     * <p><attriInfo>Its value can be set to any &lt;input&gt; tag of the form with a unique name (to the page).
     * </attriInfo>"
     */
    public String getSubmitMarker() {
        return submitMarker;
    }

    public void setSubmitMarker(String submitMarker) {
        this.submitMarker = submitMarker;
    }

    public void setCaptchaFieldName(String captchaFieldName) {
        this.captchaFieldName = captchaFieldName;
    }

    private void storeForm(final JahiaContainerList containerList,
                           final JahiaData jData)
            throws JahiaException {
        if (containerList == null) {
            logger.warn("storeForm -> null containerList passed.... Exiting");
            return;
        }
        if (containerList.getID() == 0) {
            // first let's resolve parent ACL ID
            final int parentAclID;
            if (containerList.getParentEntryID() != 0) {
                ContentContainer contentContainer = ContentContainer.
                        getContainer(containerList.getParentEntryID());
                parentAclID = contentContainer.getAclID();
            } else {
                parentAclID = jData.getProcessingContext().getPage().getAclID();
            }

            ServicesRegistry.getInstance().getJahiaContainersService().
                    saveContainerListInfo(containerList, parentAclID,
                            jData.getProcessingContext());
            final JahiaBaseACL acl = containerList.getACL();
            final JahiaAclEntry aclEntry = new JahiaAclEntry(JahiaBaseACL.WRITE_RIGHTS, JahiaACLEntry.ACL_YES);
            final JahiaUser guest = ServicesRegistry.getInstance().
                    getJahiaUserManagerService().
                    lookupUser(
                            storeAsUserName);
            final JahiaGroup guestGroup = ServicesRegistry.getInstance().
                    getJahiaGroupManagerService().
                    lookupGroup(jData.getProcessingContext().getSiteID(),
                            storeAsGroupName);

            acl.setUserEntry(guest, aclEntry);
            acl.setGroupEntry(guestGroup, aclEntry);
        }
        final JahiaContainer container = new JahiaContainer(0,
                jData.getProcessingContext().getJahiaID(),
                jData.getProcessingContext().getPageID(),
                containerList.getID(),
                0, /* rank */
                containerList.getAclID(),
                containerList.getctndefid(),
                0, 2);
        final JahiaContainerDefinition containerDef = container.getDefinition();
        ServicesRegistry.getInstance().getJahiaContainersService().
                saveContainer(container, containerList.getID(), jData.getProcessingContext());
        container.setLanguageCode(jData.getProcessingContext().getLocale().toString());
        container.fieldsStructureCheck(jData.getProcessingContext());

        final Iterator<JahiaContainerStructure> fieldDefEnum = containerDef.getStructure(
                JahiaContainerStructure.JAHIA_FIELD);
        while (fieldDefEnum.hasNext()) {
            final JahiaContainerStructure curStructureElem = (JahiaContainerStructure) fieldDefEnum.next();
            final JahiaFieldDefinition curFieldDef = (JahiaFieldDefinition) curStructureElem.getObjectDef();
            final String curFieldName = curFieldDef.getName();

            String[] curFieldValues = jData.getProcessingContext().getParameterValues(curFieldName);
            if (curFieldValues == null && curFieldDef.getAliasNames() != null) {
                // try field aliases
                String[] aliases = curFieldDef.getAliasNames();
                for (String alias : aliases) {
                    curFieldValues = jData.getProcessingContext().getParameterValues(alias);
                    if (curFieldValues != null) {
                        break;
                    }
                }
            }

            final JahiaField curField = container.getField(curFieldName);
            if (curField != null) {
                if (curField.getType() == FieldTypes.FILE) {
                    if (curFieldValues != null && curFieldValues.length > 0) {
                        final String path = curFieldValues[0];
                        final JCRNodeWrapper object = JahiaWebdavBaseService.getInstance().getDAVFileAccess(
                                path, JahiaAdminUser.getAdminUser(jData.getProcessingContext().getSiteID())
                        );
                        final JahiaFileField fField = object.getJahiaFileField();
                        curField.setValue(path);
                        curField.setObject(fField);

                        final Set<String> uris = new HashSet<String>();
                        final String[] users = jData.getProcessingContext().getParameterValues("user");
                        if (users != null) {
                            uris.addAll(Arrays.asList(users));
                        }
                        if (!uris.isEmpty()) {
                            try {
                                object.alignPermsWithField(curField, uris);
                                try {
                                    object.save();
                                } catch (RepositoryException e) {
                                    logger.error("error",e);
                                }
                            } finally {
                                try {
                                    object.refresh(false);
                                } catch (RepositoryException e) {
                                    logger.error("error",e);
                                }
                            }
                        }

                        curField.save(jData.getProcessingContext());
                    }
                } else {
                    final StringBuffer curFieldValueBuf = new StringBuffer();
                    if (curFieldValues != null) {
                        for (int i = 0; i < curFieldValues.length; i++) {
                            curFieldValueBuf.append(curFieldValues[i]);
                            if (i < (curFieldValues.length - 1)) {
                                curFieldValueBuf.append("$$$");
                            }
                        }
                    }
                    final String curFieldValue = curFieldValueBuf.toString();
                    if ((curFieldValues != null) && (!"".equals(curFieldValue))) {
                        curField.setValue(curFieldValue);
                        curField.setObject(curFieldValue);
                        curField.save(jData.getProcessingContext());
                    }
                }
            }
        }

        final JahiaEvent theEvent = new JahiaEvent(this, jData.getProcessingContext(), container);
        ServicesRegistry.getInstance().getJahiaEventService().fireAddContainer(theEvent);

        if (isImmediatePublication()) {
            if (ServicesRegistry.getInstance().getWorkflowService().getWorkflowMode(
                    containerList.getContentContainerList()) != WorkflowService.INACTIVE) {
                ServicesRegistry.getInstance().getWorkflowService().setWorkflowMode(
                        containerList.getContentContainerList(), WorkflowService.INACTIVE,
                        null, null,
                        jData.getProcessingContext());
            }
        }
        ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
    }

    public int doStartTag() {
        // Form submit handling

        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        final String submitMarkerParam;
        if (ProcessingContext.isMultipartRequest(request)) {
            final FileUpload fupload = ((ParamBean) jData.getProcessingContext()).getFileUpload();
            final String[] values = fupload.getParameterValues(getSubmitMarker());
            if (values != null && values.length > 0) {
                submitMarkerParam = fupload.getParameterValues(getSubmitMarker())[0];
            } else {
                submitMarkerParam = null;
            }
        } else {
            submitMarkerParam = request.getParameter(getSubmitMarker());
        }

        if (submitMarkerParam == null) {
            // this means we do not process the form as the submit marker is
            // not present in the request.
            return SKIP_BODY;
        }

        boolean isResponseCorrect = true;
        if (captchaFieldName != null && captchaFieldName.length() > 0) {
            isResponseCorrect = false;
            final String captchaId = request.getSession().getId();
            final String j_captcha_response = request.getParameter(captchaFieldName);
            if (logger.isDebugEnabled()) logger.debug("j_captcha_response: " + j_captcha_response);

            try {
                isResponseCorrect = CaptchaService.getInstance().validateResponseForID(captchaId, j_captcha_response);
                if (logger.isDebugEnabled()) logger.debug("CAPTCHA - isResponseCorrect: " + isResponseCorrect);
            } catch (final Exception e) {
                //should not happen, may be thrown if the id is not valid
                logger.error("Error when calling CaptchaService", e);
            }
        }

        if (isResponseCorrect && getListName() != null) {
            ContainerListBean containerListBean = (ContainerListBean) pageContext.findAttribute(getListName());
            if (containerListBean == null) {
                logger.error("Couldn't find any ContainerListBean object in context name=[" + getListName() + "]");
                return SKIP_BODY;
            }
            try {
                storeForm(containerListBean.getJahiaContainerList(), jData);
            } catch (JahiaException je) {
                logger.error("Error while processing form content mapper handler:", je);
            }
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        immediatePublication = false;
        listName = null;
        storeAsUserName = JahiaUserManagerService.GUEST_USERNAME;
        storeAsGroupName = JahiaGroupManagerService.GUEST_GROUPNAME;
        submitMarker = null;
        captchaFieldName = null;
        return EVAL_PAGE;
    }

}
