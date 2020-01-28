import java.nio.file.*

eventCleanEnd = {
    println "IMPORTANT: Removing plugin.xml files from inline plugins, because Grails doesn't regenerate plugin.xml for inline plugins."
    ant.delete(failonerror: false) {
        fileset(dir: "${basedir}/plugins") {
            include(name: "**/plugin.xml")
        }
    }
}

long runAppStartTime = 0
eventRunAppStart = {
    runAppStartTime = System.currentTimeMillis()
    println "'${new Date()}' - application is starting"
    for (pluginPath in pluginDirectories.collect { it.file.absolutePath }) {
        ant.delete(failonerror: false, dir: "$pluginPath/target")
    }
}

eventRunAppEnd = { source ->
    Properties props = new Properties(['jcatalog.application.url': "http://${serverHost ?: 'localhost'}:${serverPort}$serverContextPath"])
    File configProps = new File("./web-app/WEB-INF/conf/configuration.properties")
    if (configProps.exists()) {
        configProps.withInputStream {
            props.load(it)
        }
    }
    String appUrl = props["jcatalog.application.url"]
    if (System.getProperty("os.name")?.toLowerCase()?.contains("windows")) {
        println "Opening in browser '${appUrl}' ..."
        "cmd /c start ${appUrl}".execute()
    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
        println "Opening in browser '${appUrl}' ..."
        ['sh', '-c', "command -v xdg-open >/dev/null 2>&1 && xdg-open ${appUrl} || true"].execute()
    } else if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
        println "Opening in browser '${appUrl}' ..."
        "open ${appUrl}".execute()
    }

    println "'${new Date()}' - application started"
    println "Application startup took '${(System.currentTimeMillis() - runAppStartTime) / 1000}' seconds"
}
