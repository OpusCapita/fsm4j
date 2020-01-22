package com.opuscapita.fsm

import grails.converters.JSON

class FsmEditorApiController {
    static securedResource = "Provides access to edit FSM workflow transitions via REST interface"

    static allowedMethods = [
            edit : "GET",
            index: "GET"
    ]

    def workflowTransitionHistoryService

    def index() {
        Map searchParams = [:]

    }

    def edit() {
        String businessObjectId = params.businessObjectId
        String businessObjectType = params.businessObjectType

        Map searchParams = [
                businessObjectId  : businessObjectId,
                businessObjectType: businessObjectType
        ]

        List<WorkflowTransitionHistory> result = workflowTransitionHistoryService.search(searchParams)

        if (result.empty) {
            response.status = 404
            render([message: "Business object with ID [${businessObjectId}] and Type [${businessObjectType}] dones't exists"] as JSON)
        } else {
            WorkflowTransitionHistory editItem = result[0]

            render([
                    from            : editItem.from,
                    to              : editItem.to,
                    event           : editItem.event,
                    description     : editItem.description,
                    user            : editItem.user,
                    workflowName    : editItem.workflowName,
                    businessObjectId: editItem.businessObjId,
                    businessObjType : editItem.businessObjType
            ] as JSON)

        }
    }
}
