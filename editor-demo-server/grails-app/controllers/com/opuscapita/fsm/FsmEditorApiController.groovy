package com.opuscapita.fsm

import grails.converters.JSON

class FsmEditorApiController {

//    static allowedMethods = [
//            edit : "GET",
//            index: "GET"
//    ]

    def fsmEditorService

    def history() {
        String objectId = params.objectId
        List result = fsmEditorService.machine.getHistory([
                object: [
                        businessObjectId  : objectId,
                        businessObjectType: "invoice"
                ]
        ])

        render([history: result.findAll { it.event != "__START__" }] as JSON)
    }

    def objects() {
        List<Invoice> objects = Invoice.list()

        List result = objects.collect {
            [
                    currencyId        : it.currencyId,
                    customerId        : it.customerId,
                    grossAmount       : it.grossAmount,
                    invoiceNo         : it.invoiceNo,
                    netAmount         : it.netAmount,
                    status            : it.status,
                    supplierId        : it.supplierId,
                    vatAmount         : it.vatAmount,
                    ocfsm_demo__events: fsmEditorService.machine.availableTransitions([object: it])*.event
            ]
        }

        render(result as JSON)
    }

    def sendEvent() {
        Map vars = request.JSON as Map
        String objectId = vars.objectId
        String event = vars.event

        Invoice object = Invoice.findByInvoiceNo(objectId)
        Map result

        try {
            result = fsmEditorService.machine.sendEvent([object: object, event: event, user: "demouser", description: "Send event ${event}"])
        } catch (e) {
            log.error("Error send event", e)

            response.status = 500
            render([error: "Send event failed!"])
            return
        }

        bindData(object, result.object)

        try {
            object.save(flush: true, failOnError: true)
            render(result as JSON)
        } catch (e) {
            log.error("Error update Invoice", e)

            response.status = 500
            render([error: "Send event failed!"] as JSON)
        }

    }

    def states() {
        render([
                states: fsmEditorService.machine.machineDefinition.getAvailableStates()
        ] as JSON)
    }

    def editordata() {
        render([
                schema             : fsmEditorService.schema,
                actions            : fsmEditorService.actions,
                conditions         : fsmEditorService.conditions,
                objectConfiguration: fsmEditorService.objectConfiguration,
        ] as JSON)
    }

    def updateSchema() {
        Map vars = request.JSON as Map
        fsmEditorService.setSchema(vars.schema)

        render([status: "OK", schema: vars.schema] as JSON)
    }

    def availableTransitions() {
        Map vars = request.JSON as Map
        String objectId = vars.objectId
        Invoice object = Invoice.findByInvoiceNo(objectId)

        if (!object) {
            response.status = 404
            render([message: "Invoice by invoiceNo [${objectId}] donesn't exists"])
            return
        }

        render([
                transitions: fsmEditorService.machine.availableTransitions([
                        object: object,
                ])
        ] as JSON)
    }
}
