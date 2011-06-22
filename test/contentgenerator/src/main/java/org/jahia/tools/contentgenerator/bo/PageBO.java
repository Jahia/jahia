package org.jahia.tools.contentgenerator.bo;

import java.util.Iterator;
import java.util.List;

public class PageBO {
	private String uniqueName;
	private String titleEn;
	private String contentEn;
	private String titleFr;
	private String contentFr;
	private Integer level;
	private List<PageBO> subPages;
	private PageBO parentPage;
	private Boolean hasVanity;
	private String siteKey;
	private String fileName;
	private Integer numberBigText;

	public String getUniqueName() {
		return uniqueName;
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

	public Boolean getHasVanity() {
		return hasVanity;
	}

	public void setHasVanity(Boolean hasVanity) {
		this.hasVanity = hasVanity;
	}

	public String getSiteKey() {
		return siteKey;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getNumberBigText() {
		return numberBigText;
	}

	public void setNumberBigText(Integer numberBigText) {
		this.numberBigText = numberBigText;
	}

	public PageBO(final String pUniqueName, final String pTitleEn, final String pContentEn, final String pTitleFr,
			final String pContentFr, final int pLevel, final List<PageBO> pSubPages, Boolean pHasVanity,
			String pSiteKey, String pFileName, Integer pNumberBigText) {
		this.titleEn = pTitleEn;
		this.contentEn = pContentEn;
		this.titleFr = pTitleFr;
		this.contentFr = pContentFr;
		this.level = pLevel;
		this.subPages = pSubPages;
		this.uniqueName = pUniqueName;
		this.hasVanity = pHasVanity;
		this.siteKey = pSiteKey;
		this.fileName = pFileName;
		this.numberBigText = pNumberBigText;
	}

	public String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("	<!-- generated page (level " + this.getLevel() + ") -->\n");

		sb.append("	<"
				+ this.getUniqueName()
				+ " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:jnt=\"http://www.jahia.org/jahia/nt/1.0\" xmlns:test=\"http://www.apache.org/jackrabbit/test\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:jmix=\"http://www.jahia.org/jahia/mix/1.0\" xmlns:j=\"http://www.jahia.org/jahia/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:rep=\"internal\" changefreq=\"monthly\" j:templateNode=\"/sites/"
				+ this.getSiteKey() + "/templates/base/events\" jcr:mixinTypes=\"");
		if (this.getHasVanity()) {
			sb.append("jmix:vanityUrlMapped ");
		}
		sb.append(" jmix:sitemap\" jcr:primaryType=\"jnt:page\" priority=\"0.5\">\n"
				+ "		<j:translation_fr jcr:language=\"fr\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""
				+ this.getTitleFr()
				+ "\" />\n"
				+ "		<j:translation_en jcr:language=\"en\" jcr:mixinTypes=\"mix:title\" jcr:primaryType=\"jnt:translation\" jcr:title=\""
				+ this.getTitleEn() + "\" />\n" + "		<listA jcr:primaryType=\"jnt:contentList\">\n");
		// Big text (content)
		for (int i = 1; i <= numberBigText.intValue(); i++) {
			sb.append("			<bigText_" + i + " jcr:mixinTypes=\"jmix:renderable\" jcr:primaryType=\"jnt:bigText\">\n"
					+ "				<j:translation_fr jcr:language=\"fr\" jcr:primaryType=\"jnt:translation\" text=\""
					+ this.getContentFr() + " \" />"
					+ "				<j:translation_en jcr:language=\"en\" jcr:primaryType=\"jnt:translation\" text=\""
					+ this.getContentEn() + " \" />\n");
			sb.append("			</bigText_" + i + ">\n");
		}
		if (this.getFileName() != null) {
			sb.append(" 		<random-file jcr:primaryType=\"jnt:fileReference\">"
					+ " 			<j:translation_en  jcr:language=\"en\" jcr:primaryType=\"jnt:translation\" jcr:title=\"My file\" />"
					+ "			</random-file>");

			sb.append(" 		<publication jcr:primaryType=\"jnt:publication\">"
					+ "				<j:translation_en author=\"Jahia Content Generator\" body=\"&lt;p&gt;  Random publication&lt;/p&gt; \" date=\"01/01/1970\" file=\"/sites/"
					+ this.getSiteKey()
					+ "/files/contributed/"
					+ this.getFileName()
					+ "\" jcr:language=\"en\" jcr:primaryType=\"jnt:translation\" jcr:title=\"Random publication\" source=\"Jahia\" />"
					+ "			</publication>");
		}

		sb.append("		</listA>\n");

		if (this.getHasVanity()) {
			sb.append(" 	<vanityUrlMapping jcr:primaryType=\"jnt:vanityUrls\">"
					+ "			<_x0025_2F"
					+ this.getUniqueName()
					+ " j:active=\"true\" j:default=\"true\" j:url=\"/"
					+ this.getUniqueName()
					+ "\" jcr:language=\"en\" jcr:primaryType=\"jnt:vanityUrl\" />"
					+ "		</vanityUrlMapping>");
		}
		return sb.toString();

	}

	public String getFooter() {
		return new String("		</" + this.getUniqueName() + ">\n");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getHeader());

		if (null != this.subPages) {
			for (Iterator<PageBO> iterator = subPages.iterator(); iterator.hasNext();) {
				PageBO subPage = iterator.next();
				sb.append(subPage.toString());
			}
		}

		sb.append(this.getFooter());

		return sb.toString();
	}
}
