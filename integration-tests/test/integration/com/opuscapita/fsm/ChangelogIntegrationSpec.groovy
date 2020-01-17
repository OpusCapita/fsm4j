package com.opuscapita.fsm

import grails.test.spock.IntegrationSpec
import org.junit.After
import spock.lang.Shared

import java.text.SimpleDateFormat

class ChangelogIntegrationSpec extends IntegrationSpec {
    @Shared
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")

    @After
    void cleanup() {
        WorkflowTransitionHistory.withNewTransaction {
            WorkflowTransitionHistory.list()*.delete(flush: true)
        }
    }

    def "test changelog"() {
        when:
        WorkflowTransitionHistory.withNewTransaction {
            new WorkflowTransitionHistory([
                    from           : "a",
                    to             : "b",
                    event          : "a->b",
                    businessObjType: "salesorder",
                    businessObjId  : "test1",
                    user           : "jcadmin",
                    workflowName   : "test",
                    description    : "start",
                    finishedOn     : dateFormat.parse("18.05.2011")
            ]).save(flush: true, failOnError: true)

            new WorkflowTransitionHistory([
                    from           : "b",
                    to             : "c",
                    event          : "b->c",
                    businessObjType: "salesorder",
                    businessObjId  : "test2",
                    user           : "jcadmin",
                    workflowName   : "test",
                    description    : "move from b to c",
                    finishedOn     : dateFormat.parse("20.05.2011")
            ]).save(flush: true, failOnError: true)

            new WorkflowTransitionHistory([
                    from           : "c",
                    to             : "d",
                    event          : "c->d",
                    businessObjType: "salesorder",
                    businessObjId  : "test3",
                    user           : "test",
                    workflowName   : "test",
                    description    : "end",
                    finishedOn     : dateFormat.parse("22.05.2011")
            ]).save(flush: true, failOnError: true)
        }

        then:
        WorkflowTransitionHistory.count == 3
        WorkflowTransitionHistory.findWhere([
                from           : "a",
                to             : "b",
                event          : "a->b",
                businessObjType: "salesorder",
                businessObjId  : "test1",
                user           : "jcadmin",
                workflowName   : "test",
                description    : "start",
                finishedOn     : dateFormat.parse("18.05.2011")
        ]) != null
        WorkflowTransitionHistory.findWhere([
                from           : "b",
                to             : "c",
                event          : "b->c",
                businessObjType: "salesorder",
                businessObjId  : "test2",
                user           : "jcadmin",
                workflowName   : "test",
                description    : "move from b to c",
                finishedOn     : dateFormat.parse("20.05.2011")
        ]) != null
        WorkflowTransitionHistory.findWhere([
                from           : "c",
                to             : "d",
                event          : "c->d",
                businessObjType: "salesorder",
                businessObjId  : "test3",
                user           : "test",
                workflowName   : "test",
                description    : "end",
                finishedOn     : dateFormat.parse("22.05.2011")
        ]) != null
    }
}
