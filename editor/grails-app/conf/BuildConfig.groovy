grails.work.dir = 'target'
grails.project.target.level = 1.8
grails.project.source.level = 1.8

coverage {
    xml = true
}

codenarc.reports = {
    CodenarcXmlReport('xml') {
        outputFile = 'target/CodeNarcReport.xml'
        title = 'Codenarc Report'
    }
    CodenarcHtmlReport('html') {
        outputFile = 'target/CodeNarcReport.html'
        title = 'Codenarc Report'
    }
}

grails.plugin.location."fsm-workflow-jvm-history" = "../history"

grails.project.dependency.resolution = {
    inherits 'global'
    log 'error'
    checksums true

    dependencies {
        runtime("com.opuscapita.fsm:fsm-workflow-jvm-core:${appVersion}")
    }

    plugins {
        build('com.jcatalog.grailsplugins:build-process:7.18.GA.4',
              ':codenarc:0.19',
              ':release:3.0.1') {
            export = false
        }
        test(':code-coverage:2.0.3-3')
    }
}
