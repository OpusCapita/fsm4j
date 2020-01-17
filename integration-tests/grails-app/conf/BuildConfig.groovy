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

grails.project.dependency.resolver = "maven"
grails.plugin.location."fsm-workflow-jvm-history" = "../history"
grails.project.dependency.resolution = {
    inherits("global") {
    }
    log "error"
    checksums true



    dependencies {
        runtime('xmlunit:xmlunit:1.6')
        runtime("com.opuscapita.fsm:fsm-workflow-jvm-core:${appVersion}")

        test('com.jcatalog.maven:maven-jcatalog-db-plugin:1.2',
                'mysql:mysql-connector-java:5.1.31',
                'net.sourceforge.jtds:jtds:1.3.1',
                'com.oracle:ojdbc16:11.2.0.4')
    }

    plugins {
        runtime (':hibernate:3.6.10.18')

        runtime(':platform-core:1.0.RC6-jcatalog-20150611')
        build('com.jcatalog.grailsplugins:build-process:7.18.GA.4',
            ':codenarc:0.20',
            ':release:3.0.1')

        test(':code-coverage:2.0.3-3')

        test 'com.jcatalog.grailsplugins:jcatalog-db-migration:7.21.GA.2'
    }
}
