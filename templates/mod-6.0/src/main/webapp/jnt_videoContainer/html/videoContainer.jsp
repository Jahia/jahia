<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="videoName" var="videoName"/>
<jcr:nodeProperty node="${currentNode}" name="videoSource" var="videoSource"/>
<jcr:nodeProperty node="${currentNode}" name="videoWidth" var="videoWidth"/>
<jcr:nodeProperty node="${currentNode}" name="videoHeight" var="videoHeight"/>
<jcr:nodeProperty node="${currentNode}" name="videoHspace" var="videoHspace"/>
<jcr:nodeProperty node="${currentNode}" name="videoVspace" var="videoVspace"/>
<jcr:nodeProperty node="${currentNode}" name="videoAutostart" var="videoAutostart"/>
<jcr:nodeProperty node="${currentNode}" name="videoEnablecontextmenu" var="videoEnablecontextmenu"/>
<jcr:nodeProperty node="${currentNode}" name="videoShowstatusbar" var="videoShowstatusbar"/>
<jcr:nodeProperty node="${currentNode}" name="videoShowcontrols" var="videoShowcontrols"/>
<jcr:nodeProperty node="${currentNode}" name="videoAutosize" var="videoAutosize"/>
<jcr:nodeProperty node="${currentNode}" name="videoDisplaysize" var="videoDisplaysize"/>
<jcr:nodeProperty node="${currentNode}" name="videoLoop" var="videoLoop"/>
<jcr:nodeProperty node="${currentNode}" name="videoInvokeURLs" var="videoInvokeURLs"/>

<embed
        name='${videoName.string}'
        pluginspage="http://www.microsoft.com/Windows/MediaPlayer/"
        src="${videoSource.node.url}"
        width='${videoWidth.long}'
        height='${videoHeight.long}'
        type="application/x-mplayer2"
        autostart='${videoAutostart.string}'
        invokeURLs='${videoInvokeURLs.string}'
        enablecontextmenu='${videoEnablecontextmenu.string}'
        showstatusbar='${videoShowstatusbar.string}'
        showcontrols='${videoShowcontrols.string}'
        AutoSize='${videoAutosize.string}'
        displaysize='${videoDisplaysize.string}'
        >
</embed>
