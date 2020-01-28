grails.servlet.version = "3.0"

grails.work.dir = 'target'
grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.plugin.location."fsm-workflow-jvm-history" = "../../history"

//scan jars from classpath to resolve TLD for JSP
grails.tomcat.scan.enabled = true

grails.project.fork = [
        run: false,
        war: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 768, forkReserve: false],
        console: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 768],
        'maven-deploy': [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 768]
]

grails.project.dependency.resolution = {
    inherits("global") {
        excludes "cglib-nodep", "slf4j-log4j12", "stax-api", 'xpp3_min'
    }
    log 'error'
    checksums true

    dependencies {
        runtime('mysql:mysql-connector-java:5.1.31')
        runtime("com.opuscapita.fsm:fsm-workflow-jvm-core:${appVersion}")

        runtime 'javax.servlet:jstl:1.2'
        compile('org.grails:grails-web-databinding-spring:2.4.4')
    }

    plugins {
        runtime ":hibernate:3.6.10.18"
        runtime('org.grails.plugins:resources:1.2.1-jcatalog-20150223')
        runtime('com.jcatalog.grailsplugins:resources-extension:7.16.GA')

        build ":tomcat:7.0.54"
        build(':release:3.0.1')
        build('com.jcatalog.grailsplugins:build-process:7.18.GA.7')

        test(':code-coverage:2.0.3-3')

        runtime ':console:1.4.5'
    }
}
    