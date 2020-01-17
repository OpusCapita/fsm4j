package com.opuscapita.fsm

class WorkflowTransitionHistory {
    Long id
    String from
    String to
    String event
    String businessObjType
    String businessObjId
    String user
    String workflowName
    String description

    Date finishedOn = new Date()

    static mapping = {
        cache true

        version false

        table "WorkflowTransitionHistory"
        id column: "WorkflowTransitionHistorySN"
        from column: "`From`", length: 255
        to column: "To", length: 255
        event column: "Event", length: 255
        businessObjType column: "BusinessObjType", length: 255
        businessObjId column: "BusinessObjID", length: 255
        user column: "User", length: 255
        workflowName column: "WorkflowName", length: 255
        description column: "Description", length: 255
        finishedOn column: "FinishedOn"
    }

    static constraints = {
        from nullable: false, maxSize: 255
        to nullable: false, maxSize: 255
        event nullable: false, maxSize: 255
        businessObjId nullable: false, maxSize: 255
        businessObjType nullable: false, maxSize: 255
        user nullable: false, maxSize: 255
        workflowName nullable: false, maxSize: 255
        description nullable: false, maxSize: 255
        finishedOn nullable: false
    }
}
