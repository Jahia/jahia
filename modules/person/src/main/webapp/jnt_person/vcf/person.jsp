<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><c:set target="${renderContext}" property="contentType" value="text/vcard"/>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>

<jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
BEGIN:VCARD
VERSION:3.0
N:${currentNode.properties.lastname.string};${currentNode.properties.firstname.string}
FN:${currentNode.properties.lastname.string} ${currentNode.properties.firstname.string}
TITLE:${currentNode.properties.function.string}
ROLE:${currentNode.properties.businessUnit.string}
TEL:TYPE=WORK,VOICE:${currentNode.properties.telephone.string}
TEL;TYPE=WORK,CELL:${currentNode.properties.cellular.string}
TEL;TYPE=WORK,FAX:${currentNode.properties.fax.string}
EMAIL;TYPE=PREF,INTERNET:${currentNode.properties.email.string}
<%--
TODO add support for photo - either an absolute URL or a Base64 encoded content here 
PHOTO:TYPE=HREF,INTERNET:${picture.node.url}
--%>
REV:${currentNode.properties['jcr:lastModified'].time}
END:VCARD
