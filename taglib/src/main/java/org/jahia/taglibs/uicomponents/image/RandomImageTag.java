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
package org.jahia.taglibs.uicomponents.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;



/**
 * Class RandomImageTag : shows random image from the specified folder
 *
 * @author  Werner Assek
 *
 * @jsp:tag name="random-image" body-content="empty"
 * description="shows a random image from spcified folder
 * <p><attriInfo>
 * </attriInfo>"
 *
 */
public class RandomImageTag extends TagSupport {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final org.apache.log4j.Logger logger =
    org.apache.log4j.Logger.getLogger(RandomImageTag.class);
	
	private String path = "";
	private String fileTypes = "jpg";
	private ProcessingContext jparams = null;
	
	

	public int doStartTag()  throws JspException {

		 try {
       JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
       jparams = jData.getProcessingContext();
       Object pageCPath = pageContext.getAttribute(getPath());
       if(pageCPath != null)
      	 setPath(pageCPath.toString());
   } catch (Exception e) {
       throw new JspException("this tag is designed to be used in a JahiaContext only", e);
   }
   
   List<JCRNodeWrapper> imagelist = null;
   try {
       imagelist = getSortedImages(path, jparams, false);
   } catch (NullPointerException e) {
       logger.error("error", e);
       return SKIP_BODY;
   }
   
   List<JCRNodeWrapper> resultImgs = filterFileTypes(imagelist);
   List<String> pathes = new ArrayList<String>();
   for(int i = 0; i < resultImgs.size(); i++)
   {
  	 JCRNodeWrapper acc = resultImgs.get(i);
  	 pathes.add(acc.getUrl());
   }
   
   if(pathes.size() > 0);
   {
     try{
    	 pageContext.getOut().println("<img id=\"img" + this.hashCode() + "\" src=\"" + pathes.get(0) +"\" />");
    	 pageContext.getOut().println("<script language=\"JavaScript\"><!--\n");
    	 createJSRandomMethod(pageContext.getOut());
    	 StringBuffer js = new StringBuffer();
    	 js.append("\nimgarray").append(this.hashCode()).append(" = new Array(");
    	 for(int i = 0; i < pathes.size(); i++)
    	 {
    		 if(i > 0)
    			 js.append(", ");
    		 js.append("'").append(pathes.get(i)).append ("'");// imgarray hascode = new Array(
    	 }
    	 js.append("); ");
    	 js.append("\ndocument.getElementById('img").append(this.hashCode())
    	   .append("').src=getRandom").append(this.hashCode()).append("(imgarray")
    	   .append(this.hashCode()).append("); ");
    	 js.append("//--> </script>");
    	 pageContext.getOut().print(js.toString());
    		 
     }catch(IOException e)
     {
    	 logger.error(e,e);
     }
   }
   
	 return SKIP_BODY;
	}

	
	public int doEndTag() {

		path="";
		fileTypes = "jpg";
		return EVAL_PAGE;
  }
	
  private List<JCRNodeWrapper> getSortedImages(String webdavpath, ProcessingContext jparams, boolean thumbdisp) {
    
  	
  	JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService() ;
  	JCRNodeWrapper node = jcr.getFileNode(webdavpath, jparams.getUser());
  	
    if (!node.isCollection()) throw new NullPointerException("this path [" + webdavpath + "] is not a folder");
    // our treeset to order by name

    List<JCRNodeWrapper> images = new ArrayList<JCRNodeWrapper>();
    for (JCRNodeWrapper thefile : node.getChildren()) {
        //we dont list binaries files not image and not valid (with access denied)
        if (thefile.getName().toLowerCase().endsWith("jpg") || thefile.getName().toLowerCase().endsWith("gif")
        		 || thefile.getName().toLowerCase().endsWith("png")  || thefile.getName().toLowerCase().endsWith("bmp")) {
                images.add(thefile);
        }
    }
    return images;

}

  private List<JCRNodeWrapper> filterFileTypes(List<JCRNodeWrapper> fileList)
  {
  	List<JCRNodeWrapper> result = new ArrayList<JCRNodeWrapper>();

  	
    for(int i = 0; i < fileList.size(); i++)
    {
    	JCRNodeWrapper acc = (JCRNodeWrapper)fileList.get(i);
 
   	 String filena = acc.getName();
   	 if(filena != null && filena.indexOf(".") > 0)
   	 {
   	   String ext = filena.substring(filena.indexOf(".")+1, filena.length());
   	   if(fileTypes.toLowerCase().indexOf(ext.toLowerCase()) >= 0)
   	  	 result.add(acc);
   	 }
   	 
    }
  	return result;
  	
  }

	public String getPath()
	{
		return path;
	}


	public void setPath(String path)
	{
		this.path = path;
	}


	public String getFileTypes()
	{
		return fileTypes;
	}


	public void setFileTypes(String fileTypes)
	{
		this.fileTypes = fileTypes;
	}
	
	private void createJSRandomMethod(JspWriter out) throws IOException
	{
		StringBuffer output = new StringBuffer();
  	
  	output.append("function getRandom").append(this.hashCode()).append("(imgarray) { ");
  	output.append("var randomNum = Math.floor((imgarray.length)*(Math.random())); ");
  	output.append(" return imgarray[randomNum]; } ");
  	out.print(output.toString());
	}
}
