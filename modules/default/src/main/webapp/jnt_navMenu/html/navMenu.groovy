import org.jahia.services.content.JCRContentUtils
import org.jahia.taglibs.jcr.node.JCRTagUtils
import org.apache.taglibs.standard.functions.Functions

title = currentNode.properties['jcr:title']
baseline = currentNode.properties['j:baselineNode']
maxDepth = currentNode.properties['j:maxDepth']
startLevel = currentNode.properties['j:startLevel']
styleName = currentNode.properties['j:styleName']
layoutID = currentNode.properties['j:layoutID']
// menuItemView = currentNode.properties['j:menuItemView'] ignored

def base;
if (!baseline || baseline.string =='home') {
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
        print ("<div class=\"${styleName.string}\">")
        if (title) {
            print ("<span>${Functions.escapeXml(title.string)}</span>")
        }
        if (layoutID) {
            print("<div id=\"${layoutID.string}\">")
        }
    }

    empty = true
    if (node) {
        children = JCRContentUtils.getChildrenOfType(node, "jmix:navMenuItem")
        print (navMenuLevel == 1 ? "<div class=\"navbar\">" : "<div class=\"box-inner\">")

        print("<ul class=\"navmenu level_${navMenuLevel - startLevelValue}\">")
        children.eachWithIndex() { menuItem, index -> 
            itemPath = menuItem.path
            inpath = renderContext.mainResource.node.path == itemPath || renderContext.mainResource.node.path.startsWith(itemPath)
            selected = menuItem.isNodeType("jmix:link") ?
                renderContext.mainResource.node.path == menuItem.properties['j:node'].node.path :
                renderContext.mainResource.node.path == itemPath
            correctType = true
            if (menuItem.properties['j:displayInMenu']) {
                correctType = false
                menuItem.properties['j:displayInMenu'].each() {
                    correctType |= it.node == currentNode
                }
            }
            if ((startLevelValue < navMenuLevel || inpath) && correctType) {
                empty = false;
                hasChildren = navMenuLevel < maxDepth.long && JCRTagUtils.hasChildrenOfType(menuItem,"jnt:page,jnt:nodeLink,jnt:externalLink")
                if (startLevelValue < navMenuLevel) {
                    listItemCssClass = (hasChildren ? "hasChildren" : "") + (inpath ? " inpath" : "") + (selected ? " selected" : "") + (index == 0 ? " firstInLevel" : "") + (index == children.size()-1 ? " lastInLevel" : "") ;
                    print "<li class=\"${listItemCssClass}\">"

                    // template:module : page.menuElement.jsp - need to handle other types than page
                    title = menuItem.properties['jcr:title']
                    description = menuItem.properties['jcr:description']
                    linkTitle = description ? " title=\"${description.string}\"" : ""
                    if (menuItem.isNodeType("jnt:page")) {
                        link = menuItem.url

                        print "<a href=\"${link}\"${linkTitle}>${title.string}</a>"
                    } else if (menuItem.isNodeType("jnt:nodeLink")) {
                        reference = menuItem.properties['j:node']
                        target = menuItem.properties['j:target']
                        if (reference && reference.node) {
                            link = url.base + reference.node.path + ".html"
                            print "<a href=\"${link}\"${linkTitle} ${target ? target.string : ""}>${title.string}</a>"
                        }
                    } else if (menuItem.isNodeType("jnt:externalLink")) {
                        url = menuItem.properties['j:url']
                        target = menuItem.properties['j:target']
                        if (!url.string.startsWith("http")) {
                            print "<a href=\"http://${url.string}\" ${linkTitle} ${target ? target.string : ""}>${title.string}</a>"
                        } else {
                            print "<a href=\"${url.string}\" ${linkTitle} ${target ? target.string : ""}>${title.string}</a>"
                        }
                    }
                    // end template:module

                    if (hasChildren) {
                        printMenu(menuItem,navMenuLevel+1,true)
                    }

                    print "</li>"
                } else if (hasChildren) {
                    print "<li>"
                    printMenu(menuItem,navMenuLevel+1,true)
                    print "</li>"
                }
            }

        }
        print("</ul>")
        if (empty && renderContext.editMode) {
            print "<li class=\" selected\"><a onclick=\"return false;\" href=\"#\">Page1</a></li><li class=\"\"><a onclick=\"return false;\" href=\"#\">Page2</a></li><li class=\"\"><a onclick=\"return false;\" href=\"#\">Page3</a></li>"
        }
        print ("</div>")
    }

    if (navMenuLevel == 1) {
        print ("</div>")
    }

}

printMenu(base,1,false)

