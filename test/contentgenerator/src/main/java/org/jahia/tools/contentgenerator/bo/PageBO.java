package org.jahia.tools.contentgenerator.bo;

import java.util.Iterator;
import java.util.List;

import org.springframework.util.StringUtils;

public class PageBO {
	private Integer idPage;
	private String titleEn;
	private String contentEn;
	private Integer level;
	private List<PageBO> subPages;
	private PageBO parentPage;

	public Integer getIdPage() {
		return idPage;
	}

	public void setIdPage(Integer idPage) {
		this.idPage = idPage;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public List<PageBO> getSubPages() {
		return subPages;
	}

	public void setSubPages(List<PageBO> subPages) {
		this.subPages = subPages;
	}

	public PageBO getParentPage() {
		return parentPage;
	}

	public void setParentPage(PageBO parentPage) {
		this.parentPage = parentPage;
	}

	public String getTitleEn() {
		return titleEn;
	}

	public void setTitleEn(final String titleEn) {
		this.titleEn = titleEn;
	}

	public String getContentEn() {
		return contentEn;
	}

	public void setContentEn(final String contentEn) {
		this.contentEn = contentEn;
	}

	public PageBO(final Integer pId, final String pTitleEn, final String pContentEn, final int pLevel,
			final List pSubPages) {
		this.idPage = pId;
		this.titleEn = pTitleEn;
		this.contentEn = pContentEn;
		this.level = pLevel;
		this.subPages = pSubPages;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("	<!-- generated page -->\n");
		sb.append("	<page"
				+ this.getIdPage()
				+ " j:templateNode=\"/sites/mySite/templates/base/full-page\" jcr:created=\"2011-03-23T17:32:27.747-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-23T17:33:13.308-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:page\">\n"
				+ "		<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-23T17:32:28.054-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" jcr:title=\""
				+ this.formatForXml(this.getTitleEn())
				+ "\"/>\n"
				+ "		<maincontent jcr:created=\"2011-03-23T17:33:13.107-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-23T17:33:29.856-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "			<rich-text jcr:created=\"2011-03-23T17:33:29.797-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-23T17:33:29.856-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:bigText\">\n"
				+ "				<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-23T17:33:29.917-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" text=\"&lt;p&gt;  "
				+ this.formatForXml(this.getContentEn()) + "&lt;/p&gt; \"/>\n" 
				+ "			</rich-text>\n"
				+ "		</maincontent>\n");

		if (null != this.subPages) {
			for (Iterator<PageBO> iterator = subPages.iterator(); iterator.hasNext();) {
				PageBO subPage = (PageBO) iterator.next();
				sb.append(subPage.toString());
			}
		}
		sb.append("		</page" + this.getIdPage().toString() + ">\n");

		return sb.toString();
	}

	private String formatForXml(final String s) {
		String formattedString = StringUtils.replace(s, "&", "&amp;");
		formattedString = StringUtils.replace(formattedString, "\"", " &quot;");
		formattedString = StringUtils.replace(formattedString, "<", "&lt;");
		formattedString = StringUtils.replace(formattedString, ">", "&gt;");
		formattedString = StringUtils.replace(formattedString, "'", "&#39;");
		return formattedString;
	}
}
