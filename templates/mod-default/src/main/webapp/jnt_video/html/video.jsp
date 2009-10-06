<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="name" var="name"/>
<jcr:nodeProperty node="${currentNode}" name="source" var="source"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="width"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="height"/>
<jcr:nodeProperty node="${currentNode}" name="hspace" var="hspace"/>
<jcr:nodeProperty node="${currentNode}" name="vspace" var="vspace"/>
<jcr:nodeProperty node="${currentNode}" name="autostart" var="autostart"/>
<jcr:nodeProperty node="${currentNode}" name="enablecontextmenu" var="enablecontextmenu"/>
<jcr:nodeProperty node="${currentNode}" name="showstatusbar" var="showstatusbar"/>
<jcr:nodeProperty node="${currentNode}" name="showcontrols" var="showcontrols"/>
<jcr:nodeProperty node="${currentNode}" name="autosize" var="autosize"/>
<jcr:nodeProperty node="${currentNode}" name="displaysize" var="displaysize"/>
<jcr:nodeProperty node="${currentNode}" name="loop" var="loop"/>
<jcr:nodeProperty node="${currentNode}" name="invokeURLs" var="invokeURLs"/>

<embed
        name='${name.string}'
        pluginspage="http://www.microsoft.com/Windows/MediaPlayer/"
        src="${source.node.url}"
        width='${width.long}'
        height='${height.long}'
        type="application/x-mplayer2"
        autostart='${autostart.string}'
        invokeURLs='${invokeURLs.string}'
        enablecontextmenu='${enablecontextmenu.string}'
        showstatusbar='${showstatusbar.string}'
        showcontrols='${showcontrols.string}'
        AutoSize='${autosize.string}'
        displaysize='${displaysize.string}'
        >
</embed>
