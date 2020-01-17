package com.opuscapita.fsm

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.validation.ValidationException
import org.junit.After
import org.junit.Before
import spock.lang.Shared
import spock.lang.Specification

import java.text.SimpleDateFormat

/**
 * Specification test for WorkflowTransitionHistoryService
 */
@TestFor(WorkflowTransitionHistoryService)
@Mock(WorkflowTransitionHistory)
class WorkflowTransitionHistoryServiceSpec extends Specification {
    @Shared
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")


    @After
    void cleanup() {
        WorkflowTransitionHistory.list()*.delete(flush: true)
    }

    @Before
    void setup() {
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
    }

    def "should search by params"() {
        expect:
        service.search(searchParams)*.businessObjId == expectedObjIds

        where:
        searchParams                                        | expectedObjIds
        [:]                                                 | ["test3", "test2", "test1"]
        [from: "a"]                                         | ["test1"]
        [to: "c"]                                           | ["test2"]
        [event: "a->b"]                                     | ["test1"]
        [user: "jcadmin"]                                   | ["test2", "test1"]
        [workflowName: "test"]                              | ["test3", "test2", "test1"]
        [description: "end"]                                | ["test3"]
        [businessObjType: "salesorder"]                     | ["test3", "test2", "test1"]
        [businessObjType: "test"]                           | []
        [businessObjId: "test3"]                            | ["test3"]
        [businessObjId: "test4"]                            | []
        [finishedOn: dateFormat.parse("20.05.2011")]        | ["test2"]
        [finishedOn: [gt: dateFormat.parse("20.05.2011")]]  | ["test3"]
        [finishedOn: [gte: dateFormat.parse("20.05.2011")]] | ["test3", "test2"]
        [finishedOn: [lt: dateFormat.parse("20.05.2011")]]  | ["test1"]
        [finishedOn: [lte: dateFormat.parse("20.05.2011")]] | ["test2", "test1"]
    }

    def "should paginate by params"() {
        expect:
        service.search([:], pagingParams)*.businessObjId == expectedObjIds

        where:
        pagingParams        | expectedObjIds
        [offset: 0]         | ["test3", "test2", "test1"]
        [offset: 0, max: 2] | ["test3", "test2"]
        [max: 2]            | ["test3", "test2"]
        [offset: 1, max: 2] | ["test2", "test1"]
    }

    def "should sort by params"() {
        expect:
        service.search([:], [:], sortingParams)*.businessObjId == expectedObjIds

        where:
        sortingParams                       | expectedObjIds
        [:]                                 | ["test3", "test2", "test1"]
        [by: "finishedOn", order: "asc"]    | ["test1", "test2", "test3"]
        [order: "asc"]                      | ["test1", "test2", "test3"]
        [by: "businessObjId", order: "asc"] | ["test1", "test2", "test3"]
        [by: "event", order: "asc"]         | ["test1", "test2", "test3"]
    }

    def "should error for unsupported search fields"() {
        when:
        service.search(["noname.field": "ignore"])

        then:
        thrown(IllegalArgumentException)
    }

    def "should add new history"() {
        when:
        //invalid values, are fields is required
        service.add([:])

        then:
        thrown(ValidationException)

        when:
        def result = service.add(
                from: "open",
                to: "close",
                event: "closeSalesOrder",
                businessObjType: "salesorder",
                businessObjId: "OC01",
                user: "jcadmin",
                workflowName: "salesorder",
                description: "close sales order",
        )

        then:
        result.id != null
        result.finishedOn != null
    }

    def "should delete exist history"() {
        when:
        Integer count = service.delete([businessObjId: "test2", businessObjType: "salesorder"])

        then:
        count == 1
        WorkflowTransitionHistory.count == 2

        when:
        service.delete(["unknown.field": "error"])

        then:
        thrown(IllegalArgumentException)
    }

    def "should delete all with batch size"() {
        when:
        Integer count = service.delete([:], 1)

        then:
        count == 3
        WorkflowTransitionHistory.count == 0
    }
}
