import org.jahia.settings.SettingsBean
if (renderContext.request.session.new || renderContext.request.requestedSessionIdFromURL) {
    print ';' + SettingsBean.getInstance().getJsessionIdParameterName() + '=' + renderContext.request.session.id
}
