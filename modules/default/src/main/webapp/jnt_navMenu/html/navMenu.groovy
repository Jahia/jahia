import org.apache.taglibs.standard.functions.Functions
import org.jahia.services.content.JCRContentUtils
import org.jahia.services.render.RenderService
import org.jahia.services.render.Resource
import org.jahia.taglibs.jcr.node.JCRTagUtils

title = currentNode.properties['jcr:title']
baseline = currentNode.properties['j:baselineNode']
maxDepth = currentNode.properties['j:maxDepth']
startLevel = currentNode.properties['j:startLevel']
styleName = currentNode.properties['j:styleName']
layoutID = currentNode.properties['j:layoutID']
// menuItemView = currentNode.properties['j:menuItemView'] ignored

def base;
if (!baseline || baseline.string == 'home') {
    base = currentNode.resolveSite.home
} else if (baseline.string == 'currentPage') {
    base = JCRTagUtils.getMeAndParentsOfType(renderContext.mainResource.node, "jnt:page")[0]
}
if (!base) {
    base = renderContext.mainResource.node
}
startLevelValue = startLevel ? startLevel.long : 0


def printMenu;
printMenu = { node, navMenuLevel, omitFormatting ->
    if (navMenuLevel == 1) {
        if (styleName) {
            print("<div class=\"${styleName.string}\">")
        }
        if (title) {
            print("<span>${Functions.escapeXml(title.string)}</span>")
        }
        if (layoutID) {
            print("<div id=\"${layoutID.string}\">")
        }
    }

    empty = true

    if (node) {
        children = JCRContentUtils.getChildrenOfType(node, "jmix:navMenuItem")
        def nbOfChilds = children.size();
        def closeUl = false;
        children.eachWithIndex() { menuItem, index ->
            itemPath = menuItem.path
            inpath = renderContext.mainResource.node.path == itemPath || renderContext.mainResource.node.path.startsWith(itemPath)
            selected = menuItem.isNodeType("jmix:nodeReference") ?
                       renderContext.mainResource.node.path == menuItem.properties['j:node'].node.path :
                       renderContext.mainResource.node.path == itemPath
            correctType = true
            if (menuItem.properties['j:displayInMenu']) {
                correctType = false
                menuItem.properties['j:displayInMenu'].each() {
                    correctType |= (it.node.identifier == currentNode.identifier)
                }
            }
            if ((startLevelValue < navMenuLevel || inpath) && correctType) {
                hasChildren = navMenuLevel < maxDepth.long && JCRTagUtils.hasChildrenOfType(menuItem, "jnt:page,jnt:nodeLink,jnt:externalLink")
                if (startLevelValue < navMenuLevel) {

//                    print ("<h1>"+itemPath+closeUl+"</h1>")
                    listItemCssClass = (hasChildren ? "hasChildren" : "") + (inpath ? " inpath" : "") + (selected ? " selected" : "") + (index == 0 ? " firstInLevel" : "") + (index == nbOfChilds - 1 ? " lastInLevel" : "");
                    Resource resource = new Resource(menuItem, "html", "menuElement", currentResource.getContextConfiguration());
                    def render = RenderService.getInstance().render(resource, renderContext)
                    if (render != "") {
                        if (empty) {
                            print((navMenuLevel - startLevelValue) == 1 ? "<div class=\"navbar\">" : "<div class=\"box-inner\">")
                            print("<ul class=\"navmenu level_${navMenuLevel - startLevelValue}\">")
                            closeUl = true;
                        }
                        print "<li class=\"${listItemCssClass}\">"

                        // template:module : page.menuElement.jsp - need to handle other types than page
                        /*title = menuItem.displayableName
    //                    +index+" "+ nbOfChilds+" "+closeUl
                        description = menuItem.properties['jcr:description']
                        linkTitle = description ? " title=\"${description.string}\"" : ""
                        if (menuItem.isNodeType("jnt:page")) {
                            link = menuItem.url

                            print "<a href=\"${link}\"${linkTitle}>${title}</a>"
                        } else if (menuItem.isNodeType("jnt:nodeLink")) {
                            reference = menuItem.properties['j:node']
                            target = menuItem.properties['j:target']
                            if (reference && reference.node) {
                                link = url.base + reference.node.path + ".html"
                                print "<a href=\"${link}\"${linkTitle} ${target ? target.string : ""}>${title}</a>"
                            }
                        } else if (menuItem.isNodeType("jnt:externalLink")) {
                            url = menuItem.properties['j:url']
                            target = menuItem.properties['j:target']
                            if (!url.string.startsWith("http")) {
                                print "<a href=\"http://${url.string}\" ${linkTitle} ${target ? target.string : ""}>${title}</a>"
                            } else {
                                print "<a href=\"${url.string}\" ${linkTitle} ${target ? target.string : ""}>${title}</a>"
                            }
                        }*/


                        print render
                        // end template:module
                    }
                    if (hasChildren) {
                        printMenu(menuItem, navMenuLevel + 1, true)
                    }
                    if (render != "") {
                        print "</li>"
                        empty = false;
                    }
                } else if (hasChildren) {
//                    print "<li>"
                    printMenu(menuItem, navMenuLevel + 1, true)
//                    print "</li>"
                }
            }
            if (closeUl && index == (nbOfChilds - 1)) {
                print("</ul>");
                print("</div>")
                closeUl = false;
            }
        }

        if (empty && renderContext.editMode) {
            print "<li class=\" selected\"><a onclick=\"return false;\" href=\"#\">Page1</a></li><li class=\"\"><a onclick=\"return false;\" href=\"#\">Page2</a></li><li class=\"\"><a onclick=\"return false;\" href=\"#\">Page3</a></li>"
        }

    }

    if (navMenuLevel == 1) {
        if (layoutID) {
            print("</div>")
        }
        if (styleName) {
            print("</div>")
        }
    }

}

printMenu(base, 1, false)

