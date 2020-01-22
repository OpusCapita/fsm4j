import com.opuscapita.fsm.artefact.FsmDefinitionArtefactHandler

class FsmEditorHistoryGrailsPlugin {
    def version = "1.0.0-SNAPSHOT"
    def groupId = "com.opuscapita.grailsplugins"
    def grailsVersion = "2.4 > *"

    def title = "FSM Editor Grails Plugin"
    def author = "Dmitry Divin"
    def authorEmail = "dmitry.divin@opuscapita.com"
    def description = "The plugin provide FSM editor"

    def documentation = "http://doc.jcatalog.com/technical-documentation/workarea/docs/${groupId}/fsm-workflow-jvm/${version}/guide/single.html"

    def organization = [name: "OpusCapita Software GmbH", url: "http://www.opuscapita.com/"]
    def developers = [[name: "Dmitry Divin", email: "dmitry.divin@opuscapita.com"]]
    def issueManagement = [system: "GitHub", url: "https://github.com/OpusCapita/grails-fsm-workflow-jvm/issues"]
    def scm = [url: "git@github.com:OpusCapita/grails-fsm-workflow-jvm.git"]

    def artefacts = [FsmDefinitionArtefactHandler]
    //subscribe to watching reloading FSM definitions at runtime in development mode
    def watchedResources = [
            "file:./grails-app/fsmDefinitions/*${FsmDefinitionArtefactHandler.SUFFIX}.groovy",
            "file:./plugins/*/grails-app/fsmDefinitions/*${FsmDefinitionArtefactHandler.SUFFIX}.groovy"
    ]



}
