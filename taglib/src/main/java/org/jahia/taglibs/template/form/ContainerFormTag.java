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
            } else {
                params.put("target",ancestorContainerList.getContainerList().getContentContainerList().getUUID());
                token = FormValve.createNewToken("createNode", params);
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

            String pageUrl = page.getURL(jData.getProcessingContext());
            JspWriter out = pageContext.getOut();
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
