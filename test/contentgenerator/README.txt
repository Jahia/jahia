mvn jahia-cg:help

<profile>
	<id>cg-test</id>
	<properties>
		<jahia.cg.mysql.host>localhost</jahia.cg.mysql.host>
		<jahia.cg.mysql.login>jahia</jahia.cg.mysql.login>
		<jahia.cg.mysql.password>jahia</jahia.cg.mysql.password>
		<jahia.cg.mysql_db>jahia_content_generator</jahia.cg.mysql_db>
		<jahia.cg.mysql_table>articles</jahia.cg.mysql_table>
		<jahia.cg.nbPagesOnTopLevel>2</jahia.cg.nbPagesOnTopLevel>
		<jahia.cg.nbSubLevels>3</jahia.cg.nbSubLevels>
		<jahia.cg.nbPagesPerLevel>5</jahia.cg.nbPagesPerLevel>
		<jahia.cg.outputDirectory>/home/guillaume/Jahia/projets/contentGenerator/mvn-test/output</jahia.cg.outputDirectory>
		<jahia.cg.outputFileName>jahia-cg-output-mvn.xml</jahia.cg.outputFileName>
		<jahia.cg.createMapYn>true</jahia.cg.createMapYn>
		<jahia.cg.ouputMapName>jahia-cg.output-mvn.csv</jahia.cg.ouputMapName>
		<jahia.cg.pagesHaveVanity>true</jahia.cg.pagesHaveVanity>
		<jahia.cg.siteKey>mySite</jahia.cg.siteKey>
		<jahia.cg.addFiles>all</jahia.cg.addFiles>
		<jahia.cg.poolDirectory>/home/guillaume/Jahia/projets/contentGenerator/mvn-test/files_pool</jahia.cg.poolDirectory>
	</properties>
</profile>