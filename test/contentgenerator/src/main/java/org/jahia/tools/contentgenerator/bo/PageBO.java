package org.jahia.tools.contentgenerator.bo;

import java.util.Iterator;
import java.util.List;

import org.springframework.util.StringUtils;

public class PageBO {
	private Integer idPage;
	private String titleEn;
	private String contentEn;
	private String titleFr;
	private String contentFr;
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

	public String getTitleFr() {
		return titleFr;
	}

	public void setTitleFr(String titleFr) {
		this.titleFr = titleFr;
	}

	public String getContentFr() {
		return contentFr;
	}

	public void setContentFr(String contentFr) {
		this.contentFr = contentFr;
	}

	public PageBO(final Integer pId, final String pTitleEn, final String pContentEn, final String pTitleFr,
			final String pContentFr, final int pLevel, final List<PageBO> pSubPages) {
		this.idPage = pId;
		this.titleEn = pTitleEn;
		this.contentEn = pContentEn;
		this.titleFr = pTitleFr;
		this.contentFr = pContentFr;
		this.level = pLevel;
		this.subPages = pSubPages;
	}
	
	public String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("	<!-- generated page (level " + this.getLevel() + ") -->\n");

		sb.append("	<page"
				+ this.getIdPage()
				+ " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:jnt=\"http://www.jahia.org/jahia/nt/1.0\" xmlns:test=\"http://www.apache.org/jackrabbit/test\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:jmix=\"http://www.jahia.org/jahia/mix/1.0\" xmlns:j=\"http://www.jahia.org/jahia/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:rep=\"internal\" changefreq=\"monthly\" j:templateNode=\"/sites/ACME/templates/base/events\" jcr:created=\"2011-03-29T22:51:16.184+02:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-29T22:51:35.896+02:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:vanityUrlMapped  jmix:sitemap\" jcr:primaryType=\"jnt:page\" priority=\"0.5\">\n"
				+ "		<j:translation_fr jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""
				+ this.getTitleFr()
				+ "\" />\n"
				+ "		<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""
				+ this.getTitleEn()
				+ "\" />\n"
				+ "		<listA jcr:created=\"2011-03-28T13:00:41.415-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "			<bigText_17 j:view=\"introduction\" jcr:created=\"2011-03-28T13:00:41.415-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:renderable\" jcr:primaryType=\"jnt:bigText\">\n"
				+ "				<j:translation_fr jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" text=\""
				+ this.getContentFr()
				+ " \" />v"
				+ "				<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" text=\""
				+ this.getContentEn()
				+ " \" />\n"
				+ "			</bigText_17>\n"
				+ "		</listA>\n"
				+ "		<publications jcr:created=\"2011-03-28T13:00:41.416-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "			<publication_18 j:defaultCategory=\"/sites/systemsite/categories/Marketing /sites/systemsite/categories/us /sites/systemsite/categories/project\" jcr:created=\"2011-03-28T13:00:41.416-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:categorized\" jcr:primaryType=\"jnt:publication\">\n"
				+ "				<j:translation_fr author=\"Département marketing\" body=\"FRENCH TEXT\" date=\"02/01/2009\" jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\"Comment le groupe ACME construit des hotels prestigieux aux Etats Unis\" source=\"ACME\" />\n"
				+ "				<j:translation_en author=\"Marketing Department\" body=\"ENGLISH TEXT\" date=\"02/01/2010\" file=\"/sites/ACME/files/PDF/Publications/ACME_WP_BuildingFirstClassHotelUS.pdf\" jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\"How ACME is building First Class Hotel US\" preview=\"/sites/ACME/files/images/generic-pictures/ACME_WP_Cover.jpg\" source=\"ACME\" />\n"
				+ "			</publication_18>\n"
				+ "		</publications>\n"
				+ "		<illustration jcr:created=\"2011-03-28T13:00:41.420-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">\n"
				+ "			<imageReference j:node=\"/sites/ACME/files/images/banner-sections/publications.jpg\" jcr:created=\"2011-03-28T13:00:41.420-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:imageReference\" />\n"
				+ "		</illustration>\n"
				+ " 	<vanityUrlMapping jcr:lastModified=\"2011-03-30T15:13:23.004+02:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:vanityUrls\">"
				+ "			<_x0025_2Fpage" + this.getIdPage().toString() + " j:active=\"true\" j:default=\"true\" j:url=\"/page345\" jcr:created=\"2011-03-30T15:13:22.874+02:00\" jcr:createdBy=\" system \" jcr:language=\"en\" jcr:lastModified=\"2011-03-30T15:13:23.004+02:00\" jcr:lastModifiedBy=\"\" jcr:primaryType=\"jnt:vanityUrl\" />"
				+ "		</vanityUrlMapping>");
		return sb.toString();
	}
	
	public String getFooter() {
		return new String("		</page" + this.getIdPage().toString() + ">\n");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getHeader());

		if (null != this.subPages) {
			for (Iterator<PageBO> iterator = subPages.iterator(); iterator.hasNext();) {
				PageBO subPage = (PageBO) iterator.next();
				sb.append(subPage.toString());
			}
		}
		
		sb.append(this.getFooter());

		return sb.toString();
	}
}
