<%@ include file="common/declarations.jspf" %>

<template:template>
    <body>
    <template:containerList name="blogEntries" id="blogEntriesPagination" displayActionMenu="false">
        <template:containerForm ignoreAcl="true" var="inputs" action="${param.posturl}" name="sendForm">

            <input type="hidden" name="${inputs['date'].name}" value="${inputs['date'].defaultValue}"/>
            <input type="hidden" name="${inputs['title'].name}" value="${param.title}"/>
            <input type="hidden" name="${inputs['content'].name}" value="${param.content}"/>
            <input type="hidden" name="${inputs['keywords'].name}" value="${param.keywords}"/>
            <input type="hidden" name="${inputs['defaultCategory'].name}" value="${param.defaultCategory}"/>
            <script type="text/javascript">
                //document.sendForm.submit();
            </script>
            <input type="submit">
        </template:containerForm>
    </template:containerList>
    </body>
</template:template>