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

grails.project.dependency.resolution = {
    inherits 'global'
    log 'error'
    checksums true

    dependencies {
        compile('com.jcatalog:jcatalog-util:7.21.GA.1')
        test("com.opuscapita.fsm:fsm4j-core:${appVersion}")

        test('com.jcatalog.maven:maven-jcatalog-db-plugin:1.2',
                'mysql:mysql-connector-java:5.1.31',
                'net.sourceforge.jtds:jtds:1.3.1',
                'com.oracle:ojdbc16:11.2.0.4')
    }

    plugins {
        build('com.jcatalog.grailsplugins:build-process:7.18.GA.4',
              ':codenarc:0.19',
              ':release:3.0.1') {
            export = false
        }

        runtime ':hibernate:3.6.10.18'
        test 'com.jcatalog.grailsplugins:jcatalog-db-migration:7.21.GA.2'

        test(':code-coverage:2.0.3-3')
    }
}
