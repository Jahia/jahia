package org.jahia.tools.contentgenerator.bo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SiteBO {
	private List<PageBO> listePagesBO;

	public List<PageBO> getListePagesBO() {
		return listePagesBO;
	}

	public void setListePagesBO(List<PageBO> pages) {
		this.listePagesBO = pages;
	}

	public SiteBO() {

	}

	public SiteBO(List<PageBO> pages) {
		if (pages == null) {
			listePagesBO = new ArrayList<PageBO>();
		} else {
			this.listePagesBO = pages;	
		}
	}
	
	public String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!-- "+listePagesBO.size()+" page(s) -->\n");
		sb.append("<home xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:jnt=\"http://www.jahia.org/jahia/nt/1.0\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:jmix=\"http://www.jahia.org/jahia/mix/1.0\" xmlns:j=\"http://www.jahia.org/jahia/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:rep=\"internal\" changefreq=\"monthly\" j:templateNode=\"/sites/mySite/templates/base/home\" jcr:created=\"2011-03-23T17:28:49.681-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:32:27.814-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:sitemap\" jcr:primaryType=\"jnt:page\" priority=\"0.5\">\n"
				+ "	<header-col1 jcr:created=\"2011-03-23T17:28:49.683-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:28:59.159-04:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "		<header-left j:allowedTypes=\"\" jcr:created=\"2011-03-23T17:28:49.684-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:28:59.159-04:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:absoluteArea\"/>\n"
				+ "	</header-col1>\n"
				+ "	<header-col2 jcr:created=\"2011-03-23T17:28:49.685-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:28:59.159-04:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "		<header-right j:allowedTypes=\"\" jcr:created=\"2011-03-23T17:28:49.687-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:28:59.159-04:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:absoluteArea\"/>\n"
				+ "		</header-col2>\n"
				+ "	<header-left jcr:created=\"2011-03-23T17:28:49.688-04:00\" jcr:createdBy=\" system \" jcr:lastModified=\"2011-03-23T17:28:59.159-04:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:contentList\"/>\n");
		return sb.toString();
	}

	public String getFooter() {
		return "</home>";
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getHeader());
		
		for (Iterator<PageBO> iterator = this.getListePagesBO().iterator(); iterator.hasNext();) {
			PageBO page = (PageBO) iterator.next();
			sb.append(page.toString());
		}
		sb.append(this.getFooter());
		return sb.toString();
	}
}
