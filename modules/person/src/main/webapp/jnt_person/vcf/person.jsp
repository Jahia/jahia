<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%><%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" 
%><c:set target="${renderContext}" property="contentType" value="text/x-vcard;charset=UTF-8"/>
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
