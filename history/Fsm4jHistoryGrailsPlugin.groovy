class Fsm4jHistoryGrailsPlugin {
    def version = "1.0.1-SNAPSHOT"
    def groupId = "com.opuscapita.grailsplugins"
    def grailsVersion = "2.4 > *"

    def title = "FSM Workflow for Java History Plugin"
    def author = "Dmitry Divin"
    def authorEmail = "dmitry.divin@opuscapita.com"
    def description = "The plugin provide GORM mappings for FSM"

    def documentation = "http://doc.jcatalog.com/technical-documentation/workarea/docs/${groupId}/fsm4j/${version}/guide/single.html"

    def organization = [name: "OpusCapita Software GmbH", url: "http://www.opuscapita.com/"]
    def developers = [[name: "Dmitry Divin", email: "dmitry.divin@opuscapita.com"]]
    def issueManagement = [system: "GitHub", url: "https://github.com/OpusCapita/fsm4j/issues"]
    def scm = [url: "git@github.com:OpusCapita/fsm4j.git"]
}
