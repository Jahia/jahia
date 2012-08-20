title = currentNode.displayableName
//                    +index+" "+ nbOfChilds+" "+closeUl
description = currentNode.properties['jcr:description']
linkTitle = description ? " title=\"${description.string}\"" : ""

link = currentNode.url

print "<a href=\"${link}\"${linkTitle}>${title}</a>"