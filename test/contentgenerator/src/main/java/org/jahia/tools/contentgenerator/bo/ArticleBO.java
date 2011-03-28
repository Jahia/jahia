package org.jahia.tools.contentgenerator.bo;

public class ArticleBO {

	private Integer id;
	private String title;
	private String content;
	
	public ArticleBO() {

	}
	public ArticleBO(Integer pId, String pTitle, String pContent) {
		this.id = pId;
		this.title = pTitle;
		this.content = pContent;
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString() {
		return new String("id=" + this.id + " title=" + this.title);
	}
}
