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
package org.jahia.taglibs.template.form;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.utils.JahiaConsole;
import org.jahia.exceptions.JahiaException;
import org.jahia.operations.valves.FormValve;
import org.jahia.bin.Jahia;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import javax.jcr.Value;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 31, 2009
 * Time: 5:44:48 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ContainerFormTag extends AbstractJahiaTag {

    private static Logger logger = Logger.getLogger(JahiaConsole.class);

    private String name = "name";
    private String var = "inputs";
    private boolean ignoreAcl = false;
    private String token;
    private Map<String,Object> params;
    private String action=null;
    private boolean multipart = false;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public boolean isIgnoreAcl() {
        return ignoreAcl;
    }

    public void setIgnoreAcl(boolean ignoreAcl) {
        this.ignoreAcl = ignoreAcl;
    }

    String getToken() {
        return token;
    }

    public void setHasCaptcha(boolean hasCaptcha) {
        params.put("checkCaptcha", hasCaptcha);
    }

    public int doStartTag() {
        ServletRequest request = pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if ( jData == null )
            return EVAL_BODY_INCLUDE;

        ContainerTag ancestorContainer = (ContainerTag) findAncestorWithClass(this, ContainerTag.class, request);
        ContainerListTag ancestorContainerList = (ContainerListTag) findAncestorWithClass(this, ContainerListTag.class, request);
        boolean inContainer = ancestorContainer != null && ancestorContainer.getContainer().getListID() == ancestorContainerList.getContainerList().getID();

        JahiaPage page = jData.getProcessingContext().getPage();

        if (!ignoreAcl) {
            if (inContainer) {
                if (!ancestorContainer.getContainer().checkWriteAccess(jData.getProcessingContext().getUser())) {
                    return SKIP_BODY;
                }

            } else {
                if (!ancestorContainerList.getContainerList().checkWriteAccess(jData.getProcessingContext().getUser())) {
                    return SKIP_BODY;
                }
            }
        }

        Map<String,FormInputBean> inputs = new HashMap<String,FormInputBean>();
        pageContext.setAttribute(var, inputs);
        try {
            JahiaContainerDefinition def = ancestorContainerList.getContainerList().getDefinition();
            ExtendedNodeType nodeType = def.getNodeDefinition().getRequiredPrimaryTypes()[0];

            params = new HashMap<String,Object>();

            params.put("nodeType",nodeType.getName());
            params.put("ignoreAcl", ignoreAcl);

            if (inContainer) {
                params.put("target",ancestorContainer.getContainer().getContentContainer().getUUID());
                token = FormValve.createNewToken("updateNode", params);
                if (name.equals("name")) {
                    name = "C" + ancestorContainer.getContainer().getContentContainer().getID();
                }
            } else {
                params.put("target",ancestorContainerList.getContainerList().getContentContainerList().getUUID());
                token = FormValve.createNewToken("createNode", params);
                if (name.equals("name")) {
                    name = "CL" + ancestorContainerList.getContainerList().getContentContainerList().getID();
                }

            }

            List<ExtendedItemDefinition> items = nodeType.getItems();
            for (Iterator<ExtendedItemDefinition> iterator = items.iterator(); iterator.hasNext();) {
                ExtendedItemDefinition itemDefinition = iterator.next();

//                if (itemDefinition.getDeclaringNodeType().isNodeType("jnt:container")) {
                    if (!itemDefinition.isNode()) {
                        ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) itemDefinition;
                        Value[] defaultValues = propertyDefinition.getDefaultValues();
                        String defaultVal = "";
                        if (defaultValues.length > 0) {
                            try {
                                defaultVal = defaultValues[0].getString();
                            } catch (RepositoryException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                        inputs.put(itemDefinition.getLocalName(), new FormInputBean(propertyDefinition.getName()+token.hashCode(), defaultVal, propertyDefinition.isMandatory(),
                                Arrays.asList(propertyDefinition.getValueConstraints())));
                        if (itemDefinition.getSelector() == SelectorType.FILEUPLOAD) {
                            multipart = true;
                        }
                    } else {
                        // node
                    }
//                }

            }

            boolean isJSLoaded = Boolean.parseBoolean((String) request.getAttribute("ajaxFormLoaded"));
            JspWriter out = pageContext.getOut();
            StringBuilder buffJS = new StringBuilder();
            if (!isJSLoaded) {
                buffJS.append("<script type=\"text/javascript\">\n" +
                        "        function ");
                buffJS.append("jahiaAjaxFormToken");
                buffJS.append("(userToken,token)\n" +
                        "        {\n" +
                        "            var xhr = null;\n" +
                        "            // Creation de l'objet XMLHttpRequest\n" +
                        "            if (window.XMLHttpRequest) {\n" +
                        "                xhr = new XMLHttpRequest();\n" +
                        "            }\n" +
                        "            else if (window.ActiveXOject) {\n" +
                        "                try {\n" +
                        "                    xhr = new ActiveXObject(\"Msxml2.XMLHTTP\");\n" +
                        "                }\n" +
                        "                catch(e) {\n" +
                        "                    try {\n" +
                        "                        xhr = new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
                        "                    }\n" +
                        "                    catch(el) {\n" +
                        "                        xhr = null;\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "            else {\n" +
                        "                alert(\"Your browser does not support XMLHTTPRequest\\nplease upgrade\");\n" +
                        "                return;\n" +
                        "            }\n" +
                        "            xhr.onreadystatechange = function()\n" +
                        "            {\n" +
                        "                if (xhr.readyState == 4 && xhr.status == 200)\n" +
                        "                {\n userToken.value = xhr.responseText;\n" +
                        "                }\n" +
                        "            }\n" +
                        "            xhr.open(\"GET\",\"");
                        buffJS.append(Jahia.getContextPath());
                        buffJS.append("/tokenGenerator?formToken=\" + token, true);\n" +
                        "            xhr.setRequestHeader('Content-Type', 'x-www-form-urlencoded');\n" +
                        "            xhr.send(\"\");\n" +
                        "            }\n" +
                        "            </script>");
                request.setAttribute("ajaxFormLoaded", "true");
            }
            StringBuilder buff = new StringBuilder("<form name=\"");
            buff.append(this.name);
            if (action != null) {
              buff.append("\"" );
              buff.append(" action=\"");
              buff.append(action);                
            }
            buff.append("\" method=\"post\"");
            if (multipart) {
                buff.append(" enctype=\"multipart/form-data\"");
            }
            buff.append("><input type=\"hidden\" name=\"formToken\" value=\"");
            buff.append(token);
            buff.append("\">");
            buff.append("<input type=\"hidden\" name=\"formUserToken\" value=\"\">");
            out.print(buffJS.toString()); 
            out.print(buff.toString());
        } catch (IOException ioe) {
            logger.error("error",ioe);
        } catch (JahiaException je) {
            logger.error("error",je);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            out.print(" <script type=\"text/javascript\">\n");
            out.print("\tjahiaAjaxFormToken(document.");
            out.print(name);
            out.print(".formUserToken,\"");
            out.print(token);
            out.print("\")\n");
            out.print("</script>");
            out.print("</form>");
        } catch (IOException ioe) {
            logger.error("error",ioe);
        }

        name = "name";
        var = "inputs";
        ignoreAcl = false;
        token = null;
        action = null;
        multipart = false;

        return super.doEndTag();
    }

    public class FormInputBean {
        private String name;
        private String defaultValue;
        private boolean isMandatory;
        private List<String> constraints;
        FormInputBean() {
        }

        public FormInputBean(String name, String defaultValue, boolean mandatory, List<String> constraints) {
            this.name = name;
            this.defaultValue = defaultValue;
            isMandatory = mandatory;
            this.constraints = constraints;
        }

        public String getName() {
            return name;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean isMandatory() {
            return isMandatory;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public void setMandatory(boolean mandatory) {
            isMandatory = mandatory;
        }

        public List<String> getConstraints() {
            return constraints;
        }

        public void setConstraints(List<String> constraints) {
            this.constraints = constraints;
        }

        @Override
        public String toString() {
            return "FormInputBean{" +
                    "name='" + name + '\'' +
                    ", defaultValue='" + defaultValue + '\'' +
                    ", isMandatory=" + isMandatory +
                    '}';
        }
    }

}
