<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,workInProgress.js,admin-bootstrap.js"/>
<template:addResources type="css"
                       resources="admin-bootstrap.css,jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<fmt:message var="i18nRemoveMultipleConfirm" key="siteSettings.groups.removeMembers.confirm"/>
<fmt:message var="i18nContinue" key="label.confirmContinue"/>

<c:set var="multipleProvidersAvailable" value="${fn:length(providers) > 1}"/>
<c:set var="members" value="${group.members}"/>
<c:set var="memberCount" value="${fn:length(members)}"/>
<c:set var="membersFound" value="${memberCount > 0}"/>
<c:set var="isGroupEditable" value="${!group.properties['j:external'].boolean}"/>

<h2><fmt:message key="label.group"/>: ${fn:escapeXml(user:displayName(group))}</h2>

<script type="text/javascript">
    var addedMembers = []
    var removedMembers = []
    $(document).ready(function() {
        $(".selectedMember").change(function(event) {
            v = $(this).val();

            name = '${prefix}' + $(this).attr('value');
            if ($(this).is(':checked')) {
                if (removedMembers.indexOf(name) > -1) {
                    removedMembers.splice(removedMembers.indexOf(name),1)
                } else {
                    addedMembers[addedMembers.length] = name
                }
            } else {
                if (addedMembers.indexOf(name) > -1) {
                    addedMembers.splice(addedMembers.indexOf(name),1)
                } else {
                    removedMembers[removedMembers.length] = name
                }
            }

            if (addedMembers.length == 0 && removedMembers.length == 0) {
                $('#saveButton').attr('disabled', 'disabled')
            } else {
                $('#saveButton').removeAttr("disabled")
            }
//            if ($(this).is(':checked') && $('#removedMembers'))})
        })

        $('#cbSelectedAllMembers').click(function() {
            var state=this.checked;
            $.each($(':checkbox[name="selectedMembers"]'), function() {
                if (this.checked != state) {
                    this.checked = state;
                    $(this).change()
                }
            });
        });

        $("#saveForm").submit(function() {
            workInProgress('${i18nWaiting}');
            $("#addedMembers").val(addedMembers)
            $("#removedMembers").val(removedMembers)
        })
    })

</script>

<div>
    <form action="${flowExecutionUrl}" method="post" style="display: inline;">
        <div>
            <button class="btn" type="submit" name="_eventId_editGroup">
                <i class="icon-arrow-left"></i>
                &nbsp;<fmt:message key="siteSettings.label.backToGroup"/>
            </button>
            <button class="btn" type="submit" name="_eventId_editUsers">
                <img src="<c:url value='/modules/assets/css/img/icon-user-small.png'/>" alt=""/>
                &nbsp;<fmt:message key="label.users"/>
            </button>

            <button class="btn" type="submit" name="_eventId_editGroups">
                <img src="<c:url value='/modules/assets/css/img/icon-group-small.png'/>" alt=""/>
                &nbsp;<fmt:message key="label.groups"/>
            </button>
        </div>
    </form>
</div>