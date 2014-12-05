package org.jahia.perftest

import net.htmlparser.jericho.CharacterReference
import net.htmlparser.jericho.HTMLElementName
import net.htmlparser.jericho.Source
import org.jahia.api.Constants
import org.jahia.registries.ServicesRegistry
import org.jahia.services.SpringContextSingleton
import org.jahia.services.content.*
import org.jahia.services.content.decorator.JCRGroupNode
import org.jahia.services.content.decorator.JCRSiteNode
import org.jahia.services.seo.VanityUrl
import org.jahia.services.seo.jcr.VanityUrlManager

import javax.jcr.RepositoryException

class ContentGenerator {
    def languagesList = ["sq", "ar", "be", "bg", "ca", "zh", "hr", "cs", "da", "nl", "et", "fi", "fr", "de", "el", "iw", "hu", "is", "in", "it", "ja"];

    def nbContents = 0

    def percentageExpiration = 20
    def percentagePerUser = 33

    def aclsOn1stpage = false;
    def aclsOn2ndpage = false;
    def aclsOn3rdpage = false;
    def withExpiration = false;

    def nbOfGroupsPerLevel = 1
    def randomgroupName = new Random()
    def nbUsersPerGroup = 10

    def nbUsersToCreate = 10

    /**
     * Use the lipsum generator to generate Lorem Ipsum dummy paragraphs / words / bytes.
     *
     * Lorem Ipsum courtesy of www.lipsum.com by James Wilson
     *
     * @param what in ['paras','words','bytes'], default: 'paras'
     * @param amount of paras/words/bytes, default: 2 (for words minimum is 5, for bytes it is 27)
     * @param start always start with 'Lorem Ipsum', default = true
     * */
    def lipsum = { what = "paras", amount = 2, start = true ->
        def text = new URL("http://www.lipsum.com/feed/xml?what=$what&amount=$amount&start=${start ? 'yes' : 'no'}").text

        def feed = new XmlSlurper().parseText(text)

        feed.lipsum.text()
    }

    /**
     * Use the randomTextGenerator to generate language specific text.
     * */
    def randomTextGeneratorLanguage = ['en', 'cn', 'nl', 'fin', 'fr', 'de', 'el', 'il', 'it', 'jp', 'ltn', 'pl', 'pt', 'ru', 'sr', 'es']

    def randomTextGenerator = { language = 'en' ->
        when:
        final URLConnection connection = new URL('http://randomtextgenerator.com/').openConnection()
        connection.setDoOutput(true)
        connection.outputStream.withWriter { Writer writer -> writer << 'text_mode=plain&language=' + language }
        String response = connection.inputStream.withReader { Reader reader -> reader.text }
        then:
        def source = new Source(response)
        CharacterReference.decodeCollapseWhiteSpace(source.getFirstElement(HTMLElementName.TEXTAREA).getContent())
    }

    def rand = new Random();

    def addExpiration = { JCRNodeWrapper node ->
        if (withExpiration) {
            if (nbContents % percentageExpiration == 0) {
                node.addMixin("jmix:cache")
                node.setProperty("j:expiration", rand.nextInt(10))
            }
            if ((nbContents) % percentagePerUser == 0) {
                node.addMixin("jmix:cache")
                node.setProperty("j:perUser", true)
                node.setProperty("j:expiration", rand.nextInt(5))
            }
        }
    }

    def rolesDefinition = [new HashSet<String>(["editor-in-chief"]), new HashSet<String>(["editor"]), new HashSet<String>(["owner"]), new HashSet<String>(["reviewer"]), new HashSet<String>(["contributor"])]
    def addAcl = { JCRNodeWrapper node, groupName -> node.grantRoles(groupName, rolesDefinition[rand.nextInt(rolesDefinition.size())]) }

    def createContent = { JCRNodeWrapper page, int nbRow, int nbText ->
        println "Create content for " + page.getPath()
        def random = new Random();
        JCRNodeWrapper area = page.getNode("pagecontent")
        JCRNodeWrapper row = area.getNode("bootstrap-row");
        (1..nbRow).each {
            row.copy(area, "bootstrap-row" + it, false)
            nbContents++;
            JCRNodeWrapper newRow = area.getNode("bootstrap-row" + it);
            JCRNodeWrapper list = newRow.getNode("bootstrap-column").addNode("bootstrap-column", "jnt:contentList");
            (1..nbText).each { idx ->
                nbContents++;
                def i = random.nextInt(randomTextGeneratorLanguage.size())
                def newNode = list.addNode("simple-text" + idx, "jnt:text")
                try {
                    //newNode.setProperty("text", randomTextGenerator(randomTextGeneratorLanguage[i]).toString())
                    newNode.setProperty("text", "" + lipsum('paras', 2, false).toString())
                    addExpiration(newNode)
                } catch (Exception e) {
                }
            }
            list = newRow.getNode("bootstrap-column-1").addNode("bootstrap-column-1", "jnt:contentList");
            (1..nbText).each { idx ->
                nbContents++;
                def newNode = list.addNode("simple-text" + idx, "jnt:text")
                try {
                    //newNode.setProperty("text", randomTextGenerator(randomTextGeneratorLanguage[i]).toString())
                    newNode.setProperty("text", "" + lipsum('paras', 2, false).toString())
                    addExpiration(newNode)
                } catch (Exception e) {
                }
            }
        }

    }

    def updateContent = { JCRNodeWrapper page, int nbRow, int nbText, locale ->
        def random = new Random();
        JCRNodeWrapper area = page.getNode("pagecontent")
        (1..nbRow).each {
            try {
                JCRNodeWrapper newRow = area.getNode("bootstrap-row" + it);
                JCRNodeWrapper list = newRow.getNode("bootstrap-column").getNode("bootstrap-column");
                (1..nbText).each { idx ->
                    def i = random.nextInt(randomTextGeneratorLanguage.size())
                    try {
                        list.getNode("simple-text" + idx).setProperty("text",
                                locale + " " + randomTextGenerator(randomTextGeneratorLanguage[i]))
                    } catch (Exception e) {
                    }
                }
                list = newRow.getNode("bootstrap-column-1").getNode("bootstrap-column-1");
                (1..nbText).each { idx ->
                    try {
                        list.getNode("simple-text" + idx).setProperty("text", locale + " " + lipsum('paras', 2, false).toString())
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    def VanityUrlManager vanityUrlManager = (VanityUrlManager) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlManager")

    def generateContent = {
        siteName, nbPagesFirstLevel, nbPagesSecondLevel, nbPagesThirdLevel, nbRowPerPage, nbTextPerPage, randomElements = false, publishFirstLevelOnEachIteration = false, createVanity = true ->
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            def randomEl = new Random();
                            JCRNodeWrapper home = session.getNode("/sites/" + siteName + "/home");
                            JCRNodeWrapper page1 = home.getNode("page");
                            def int nbPage = 0;

                            (1..nbPagesFirstLevel).each { it1 ->
                                if (page1.copy(home, "page" + (++nbPage), false)) {
                                    nbContents++;
                                    JCRNodeWrapper pageLvl1 = home.getNode("page" + (nbPage));
                                    pageLvl1.setProperty("jcr:title", "Page number " + (nbPage))
                                    vanityUrlManager.saveVanityUrlMapping(pageLvl1, new VanityUrl("/pages" + siteName + "/page_en_" + nbPage, siteName, "en", true, true), session)
                                    def groupName = "g:group_" + randomgroupName.nextInt(nbOfGroupsPerLevel + 1)
                                    if (aclsOn1stpage) {
                                        addAcl(pageLvl1, groupName)
                                    }
                                    println "level 1" + pageLvl1.getPath()
                                    (1..nbPagesSecondLevel).each { it2 ->
                                        if (page1.copy(pageLvl1, "page" + (++nbPage), false)) {
                                            nbContents++;
                                            JCRNodeWrapper pageLvl2 = pageLvl1.getNode("page" + nbPage);
                                            pageLvl2.setProperty("jcr:title", "Page number " + (nbPage))
                                            vanityUrlManager.saveVanityUrlMapping(pageLvl2, new VanityUrl("/pages" + siteName + "/page_en_" + nbPage, siteName, "en", true, true), session)
                                            def subgroupName = groupName + "_" + randomgroupName.nextInt(nbOfGroupsPerLevel + 1)
                                            if (aclsOn2ndpage) {
                                                addAcl(pageLvl2, subgroupName)
                                            }
                                            println "level 2" + pageLvl2.getPath()
                                            (1..nbPagesThirdLevel).each { it3 ->
                                                if (page1.copy(pageLvl2, "page" + (++nbPage), false)) {
                                                    nbContents++;
                                                    JCRNodeWrapper pageLvl3 = pageLvl2.getNode("page" + nbPage);
                                                    pageLvl3.setProperty("jcr:title", "Page number " + (nbPage))
                                                    vanityUrlManager.saveVanityUrlMapping(pageLvl3, new VanityUrl("/pages" + siteName + "/page_en_" + nbPage, siteName, "en", true, true), session)
                                                    def subsubgroupName = subgroupName + "_" + randomgroupName.nextInt(nbOfGroupsPerLevel + 1)
                                                    if (aclsOn3rdpage) {
                                                        addAcl(pageLvl3, subsubgroupName)
                                                    }
                                                    println "level 3" + pageLvl3.getPath()
                                                    createContent(pageLvl3, (randomElements ? randomEl.nextInt(nbRowPerPage) : nbRowPerPage),
                                                            (randomElements ? randomEl.nextInt(nbTextPerPage) : nbTextPerPage));
                                                    session.save();
                                                }
                                            }
                                            createContent(pageLvl2,
                                                    (randomElements ? randomEl.nextInt(nbRowPerPage) : nbRowPerPage),
                                                    (randomElements ? randomEl.nextInt(nbTextPerPage) : nbTextPerPage));
                                            session.save();
                                        }

                                    }
                                    createContent(pageLvl1,
                                            (randomElements ? randomEl.nextInt(nbRowPerPage) : nbRowPerPage),
                                            (randomElements ? randomEl.nextInt(nbTextPerPage) : nbTextPerPage));
                                    session.save();
                                    if (publishFirstLevelOnEachIteration) {
                                        ServicesRegistry.instance.JCRPublicationService.publishByMainId(pageLvl1.identifier)
                                    }
                                }
                            }
                            return null;
                        }
                    });
    }


    def updateContentForLanguages = {
        siteName, nbPagesFirstLevel, nbPagesSecondLevel, nbPagesThirdLevel, nbRowPerPage, nbTextPerPage, languages = languagesList, publishFirstLevelOnEachIteration = false ->
            languages.each { language ->
                locale = new java.util.Locale(language);
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, locale,
                        new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                println session.locale.displayName;
                                JCRNodeWrapper home = session.getNode("/sites/" + siteName + "/home");
                                def int nbPage = 1;
                                (1..nbPagesFirstLevel).each { it1 ->
                                    JCRNodeWrapper pageLvl1 = home.getNode("page" + nbPage);
                                    if (!pageLvl1.hasNode("j:translation_" + session.locale)) {
                                        pageLvl1.setProperty("jcr:title",
                                                "Page number " + (nbPage++) + " " + session.locale.getDisplayName(session.locale))
                                        vanityUrlManager.saveVanityUrlMapping(pageLvl1, new VanityUrl("/pages/page_" + session.locale + "_" + nbPage, siteName, session.locale.toString(), true, true), session)
                                        (1..nbPagesSecondLevel).each { it2 ->
                                            JCRNodeWrapper pageLvl2 = pageLvl1.getNode("page" + nbPage);
                                            if (!pageLvl2.hasNode("j:translation_" + session.locale)) {
                                                pageLvl2.setProperty("jcr:title",
                                                        "Page number " + (nbPage++) + " " + session.locale.getDisplayName(session.locale))
                                                vanityUrlManager.saveVanityUrlMapping(pageLvl2, new VanityUrl("/pages/page_" + session.locale + "_" + nbPage, siteName, session.locale.toString(), true, true), session)
                                                (1..nbPagesThirdLevel).each { it3 ->
                                                    JCRNodeWrapper pageLvl3 = pageLvl2.getNode("page" + nbPage);
                                                    if (!pageLvl3.hasNode("j:translation_" + session.locale)) {
                                                        pageLvl3.setProperty("jcr:title", "Page number " + (nbPage++) + " " + session.locale.getDisplayName(session.locale))
                                                        vanityUrlManager.saveVanityUrlMapping(pageLvl3, new VanityUrl("/pages/page_" + session.locale + "_" + nbPage, siteName, session.locale.toString(), true, true), session)
                                                        updateContent(pageLvl3, nbRowPerPage, nbTextPerPage,
                                                                session.locale.displayName);
                                                        session.save();
                                                    }
                                                }
                                                updateContent(pageLvl2, nbRowPerPage, nbTextPerPage, session.locale.displayName);
                                                session.save();
                                            }
                                        }
                                        updateContent(pageLvl1, nbRowPerPage, nbTextPerPage, session.locale.displayName);
                                        session.save();
                                        if (publishFirstLevelOnEachIteration) {
                                            ServicesRegistry.instance.JCRPublicationService.publishByMainId(pageLvl1.identifier)
                                        }
                                    }
                                }
                                return null;
                            }
                        });
            }
    }

    def createGroups = { String site = null ->
        def randUsers = new Random()
        def service = ServicesRegistry.instance.jahiaGroupManagerService
        def userService = ServicesRegistry.instance.jahiaUserManagerService
        if (service.lookupGroup(site, "group_" + (site != null ? site + "_" : "") + "0_0_0") == null) {
            def session = JCRSessionFactory.instance.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH)
            (0..nbOfGroupsPerLevel).each { first ->
                (0..nbOfGroupsPerLevel).each { second ->
                    (0..nbOfGroupsPerLevel).each {
                        def JCRGroupNode group = (JCRGroupNode) service.createGroup(site,
                                "group_" + (site != null ? site + "_" : "") + first + "_" + second + "_" + it, null,
                                false, session)
                        if (group != null) {
                            (0..nbUsersPerGroup).each {
                                def user = userService.lookupUser("userGroovy_" + (site != null ? site + "_" : "") + randUsers.nextInt(nbUsersToCreate), site, session)
                                if (user != null) {
                                    group.addMember(user)
                                }
                            }
                        }
                    }
                }
            }
            session.save()
            (0..nbOfGroupsPerLevel).each { first ->
                (0..nbOfGroupsPerLevel).each { second ->
                    def JCRGroupNode group = (JCRGroupNode) service.createGroup(site, "group_" + (site != null ? site + "_" : "") + first + "_" + second, null,
                            false, session)
                    (0..3).each {
                        if (group != null) {
                            group.addMember(service.lookupGroup(site,
                                    "group_" + (site != null ? site + "_" : "") + first + "_" + second + "_" + randUsers.nextInt(nbOfGroupsPerLevel + 1)))
                        }
                    }
                }
            }
            session.save()
            (0..nbOfGroupsPerLevel).each { first ->
                def JCRGroupNode group = (JCRGroupNode) service.createGroup(site, "group_" + (site != null ? site + "_" : "") + first, null, false, session)
                (0..3).each {
                    if (group != null) {
                        group.addMember(service.lookupGroup(site, "group_" + (site != null ? site + "_" : "") + first + "_" + randUsers.nextInt(nbOfGroupsPerLevel + 1)))
                    }
                }
            }
            session.save()
        }
    }

    def createUsers = { String site = null ->
        def userService = ServicesRegistry.instance.jahiaUserManagerService
        if (userService.lookupUser("userGroovy_" + (site != null ? site + "_0" : "0")) == null) {
            def session = JCRSessionFactory.instance.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH)
            def properties = new java.util.Properties();
            (0..nbUsersToCreate).each { first ->
                if (site == null)
                    userService.createUser("userGroovy_" + first, "password", properties, session);
                else
                    userService.createUser("userGroovy_" + site + "_" + first, site, "password", properties, session);
                if (first % 100 == 0)
                    session.save()
            }
            session.save()
        }
    }

    def modules = ["tags"]

    def createSite = { siteKey ->
        def sitesService = ServicesRegistry.instance.jahiaSitesService
        if (sitesService.getSiteByKey(siteKey) == null) {
            def session = JCRSessionFactory.instance.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH)
            def user = ServicesRegistry.instance.jahiaUserManagerService.lookupUser("root", session)
            def site = sitesService.addSite(user.jahiaUser, siteKey, "www." + siteKey + ".org.dev", siteKey, siteKey, Locale.ENGLISH, "sample-bootstrap-templates", modules.toArray(new String[modules.size()]), null, null, null, false, false, null)
            def JCRSiteNode siteNode = session.getNode(site.getJCRLocalPath())
            def page = siteNode.getHome().addNode("page", "jnt:page")
            page.setProperty("j:templateName", "1col")
            page.setProperty("jcr:title", "Default Empty Page")
            def area = page.addNode("pagecontent", "jnt:contentList")
            def row = area.addNode("bootstrap-row", "jnt:bootstrapRow")
            row.setProperty("fluid", false)
            def column = row.addNode("bootstrap-column", "jnt:bootstrapColumn")
            column.setProperty("span", 8)
            column.setProperty("offset", 0)
            column = row.addNode("bootstrap-column-1", "jnt:bootstrapColumn")
            column.setProperty("span", 4)
            column.setProperty("offset", 0)
            session.save()
            ServicesRegistry.instance.JCRPublicationService.publishByMainId(site.identifier)
            return true
        }
        return false
    }
}
