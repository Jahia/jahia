package org.jahia.taglibs.template.users;

import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a helper tag that will only display the body if the user property is public and the current user is not
 * the displayed user.
 *
 * @author loom
 *         Date: Sep 18, 2009
 *         Time: 8:31:43 AM
 */
public class IsPublicTag extends BodyTagSupport {
    private String userKey;
    private String property;
    private Boolean invert;

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setInvert(Boolean invert) {
        this.invert = invert;
    }

    @Override
    public int doStartTag() throws JspException {
        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        if (renderContext == null) {
            final JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
            renderContext = new RenderContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), jData.getProcessingContext().getUser());
            renderContext.setSite(jData.getProcessingContext().getSite());
        }
        JahiaUser currentUser = renderContext.getUser();
        JahiaUser displayedUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userKey);
        if (currentUser.equals(displayedUser)) {
            // in the case of the current user we always return true.
            return super.doStartTag();
        }
        String displayPropertyPrivate = displayedUser.getProperty(property + "Public");
        boolean willExecuteBody = true;
        if (displayPropertyPrivate != null) {
            Boolean displayPropertyBoolean = new Boolean(displayPropertyPrivate);
            if (displayPropertyBoolean.booleanValue()) {
                willExecuteBody = true;
            } else {
                willExecuteBody = false;
            }
        }
        if (invert != null) {
            if (invert.booleanValue()) {
                willExecuteBody = !willExecuteBody;
            }
        }
        if (willExecuteBody) {
            return super.doStartTag();
        } else {
            return SKIP_BODY;
        }
    }

    @Override
    public int doAfterBody() throws JspException {
      try {
         BodyContent bodycontent = getBodyContent();
         String body = bodycontent.getString();
         JspWriter out = bodycontent.getEnclosingWriter();
         if(body != null) {
            out.print(body);
         }
      } catch(IOException ioe) {
         throw new JspException("Error:"+ioe.getMessage());
      }
      return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        userKey = null;
        property = null;
        return super.doEndTag();
    }

}
