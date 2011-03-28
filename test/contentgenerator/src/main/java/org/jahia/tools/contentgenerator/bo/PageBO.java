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
			final List<PageBO> pSubPages) {
		this.idPage = pId;
		this.titleEn = pTitleEn;
		this.contentEn = pContentEn;
		this.level = pLevel;
		this.subPages = pSubPages;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("	<!-- generated page (level " + this.getLevel() + ") -->\n");
	
		sb.append("	<page"
				+ this.getIdPage()
				+ " j:templateNode=\"/sites/ACME/templates/base/publications\" jcr:created=\"2011-03-28T13:00:41.414-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:sitemap\" jcr:primaryType=\"jnt:page\" priority=\"0.5\">"
				+ "		<j:translation_fr jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""+ this.getTitleEn() + "\" />"
				+ "		<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""+ this.getTitleEn() + "\" />"
				+ "		<listA jcr:created=\"2011-03-28T13:00:41.415-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">"
				+ "			<bigText_17 j:view=\"introduction\" jcr:created=\"2011-03-28T13:00:41.415-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:renderable\" jcr:primaryType=\"jnt:bigText\">"
				+ "				<j:translation_fr jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" text=\"" + this.getContentEn() + " \" />"
				+ "				<j:translation_en jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:translation\" text=\"" + this.getContentEn() + " \" />"
				+ "			</bigText_17>"
				+ "		</listA>"
				+ "		<publications jcr:created=\"2011-03-28T13:00:41.416-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">"
				+ "			<publication_18 j:defaultCategory=\"/sites/systemsite/categories/Marketing /sites/systemsite/categories/us /sites/systemsite/categories/project\" jcr:created=\"2011-03-28T13:00:41.416-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"jmix:categorized\" jcr:primaryType=\"jnt:publication\">"
				+ "				<j:translation_fr author=\"Département marketing\" body=\"FRENCH TEXT\" date=\"02/01/2009\" jcr:language=\"fr\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\"Comment le groupe ACME construit des hotels prestigieux aux Etats Unis\" source=\"ACME\" />"
				+ "				<j:translation_en author=\"Marketing Department\" body=\"ENGLISH TEXT\" date=\"02/01/2010\" file=\"/sites/ACME/files/PDF/Publications/ACME_WP_BuildingFirstClassHotelUS.pdf\" jcr:language=\"en\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\"How ACME is building First Class Hotel US\" preview=\"/sites/ACME/files/images/generic-pictures/ACME_WP_Cover.jpg\" source=\"ACME\" />"
				+ "			</publication_18>"
				+ "		</publications>"
				+ "		<illustration jcr:created=\"2011-03-28T13:00:41.420-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:contentList\">"
				+ "			<imageReference j:node=\"/sites/ACME/files/images/banner-sections/publications.jpg\" jcr:created=\"2011-03-28T13:00:41.420-04:00\" jcr:createdBy=\"root\" jcr:lastModified=\"2011-03-28T13:01:06.712-04:00\" jcr:lastModifiedBy=\"root\" jcr:primaryType=\"jnt:imageReference\" />"
				+ "		</illustration>");


		if (null != this.subPages) {
			for (Iterator<PageBO> iterator = subPages.iterator(); iterator.hasNext();) {
				PageBO subPage = (PageBO) iterator.next();
				sb.append(subPage.toString());
			}
		}
		sb.append("		</page" + this.getIdPage().toString() + ">\n");

		return sb.toString();
	}
}
