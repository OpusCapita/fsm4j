package com.opuscapita.fsm

import com.opuscapita.fsm.example.InvoiceReceipt
import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import org.junit.After

/**
 * Integration tests the Machine with History service
 *
 * @author Dmitry Divin
 */
class MachineIntegrationSpec extends IntegrationSpec {
    WorkflowTransitionHistoryService workflowTransitionHistoryService

    @After
    void cleanup() {
        WorkflowTransitionHistory.withNewTransaction {
            WorkflowTransitionHistory.list()*.delete(flush: true)
        }
    }

    def "should processing workflow"() {
        given:
        def result
        String maintainerUser = "maintainer"
        String approverUser = "approver"
        def schema = new JsonSlurper().parse(new File("./test/integration/com/opuscapita/fsm/default-schema.json"))
        MachineDefinition machineDefinition = new MachineDefinition([
                schema             : schema,
                objectConfiguration: [
                        stateFieldName: "approvalStatus"
                ]
        ])
        Machine machine = new Machine([
                machineDefinition       : machineDefinition,
                history                 : workflowTransitionHistoryService,
                convertObjectToReference: { InvoiceReceipt invoiceReceipt ->
                    [businessObjType: "invoice", businessObjId: invoiceReceipt.invoiceReceiptId]
                }
        ])

        when:
        InvoiceReceipt object = new InvoiceReceipt(invoiceReceiptId: "IN01")

        then:
        !machine.isRunning([object: object])

        when:
        result = machine.start([object: object, user: maintainerUser, description: "Start workflow for invoice ${object.invoiceReceiptId}"])

        then:
        result != null
        object.approvalStatus == "inspectionRequired"

        machine.isRunning([object: object])
        machine.is([object: object, state: "inspectionRequired"])

        WorkflowTransitionHistory.count == 1
        WorkflowTransitionHistory.findWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : "maintainer",
                description    : "Start workflow for invoice IN01",
                workflowName   : "InvoiceApproval"
        ]) != null

        when:
        result = machine.sendEvent([object: object, event: "automatic-inspect", user: maintainerUser, description: "Automatic inspection"])

        then:
        result != null
        object.approvalStatus == "approvalRequired"

        machine.isRunning([object: object])
        machine.is([object: object, state: "approvalRequired"])

        WorkflowTransitionHistory.count == 2
        WorkflowTransitionHistory.findWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : "maintainer",
                description    : "Start workflow for invoice IN01",
                workflowName   : "InvoiceApproval"
        ]) != null
        WorkflowTransitionHistory.findWhere([
                from           : "inspectionRequired",
                to             : "approvalRequired",
                event          : "automatic-inspect",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : "maintainer",
                description    : "Automatic inspection",
                workflowName   : "InvoiceApproval"
        ]) != null

        when:
        result = machine.sendEvent([object: object, event: "approve", user: approverUser, description: "Approve"])

        then:
        result != null
        object.approvalStatus == "approved"

        !machine.isRunning([object: object])
        machine.is([object: object, state: "approved"])

        WorkflowTransitionHistory.count == 3
        WorkflowTransitionHistory.findWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : "maintainer",
                description    : "Start workflow for invoice IN01",
                workflowName   : "InvoiceApproval"
        ]) != null
        WorkflowTransitionHistory.findWhere([
                from           : "inspectionRequired",
                to             : "approvalRequired",
                event          : "automatic-inspect",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : "maintainer",
                description    : "Automatic inspection",
                workflowName   : "InvoiceApproval"
        ]) != null
        WorkflowTransitionHistory.findWhere([
                from           : "approvalRequired",
                to             : "approved",
                event          : "approve",
                businessObjId  : "IN01",
                businessObjType: "invoice",
                user           : approverUser,
                description    : "Approve",
                workflowName   : "InvoiceApproval"
        ]) != null
    }
}
