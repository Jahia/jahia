This tool will create an XML file containing n pages created from ACME "Publications" page, and filled with a random article content picked from Wikipedia data.

Usage:
jahia_cg_nbPagesOnTopLevel=1
jahia_cg_nbSubLevels=2
jahia_cg_nbPagesPerLevel=3

These properties will result to the following arborescence:

top-level-page
	|- sub-page1
		|-subpage11
		|-subpage12
		|-subpage13
	|- sub-page2	
		|-subpage21
		|-subpage22
		|-subpage23
	|- sub-page3
		|-subpage31
		|-subpage32
		|-subpage33
Total: 13 pages

You can generate a maximum total of 200,000 pages. A maximum of 10,000 articles will be selected from the database, and randomly inserted in the generated XML code.