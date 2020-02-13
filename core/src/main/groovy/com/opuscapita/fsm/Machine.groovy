package com.opuscapita.fsm

import groovy.util.logging.Log


/**
 * Machine
 *
 * @author Alexy Sergeev
 * @author Dmitry Divin
 */
@Log
class Machine {
    private def machineDefinition
    private def context
    private def history
    private Closure convertObjectToReference

    /**
     * The constructor
     *
     * @param machineDefinition - (required) instance of type MachineDefinition
     * @param context - (optional) context object usage
     * @param history - (optional) history service, by default mock usage
     */
    Machine(params) {
        this.machineDefinition = params.machineDefinition
        this.context = params.context
        this.history = params.history

        assert this.machineDefinition != null, "Parameter [machineDefinition] is mandatory"
        // default implementation will throw an exception which should inform
        // developer that machine is not properly configured
        this.convertObjectToReference = params.convertObjectToReference ?: {
            throw new Exception("""
'convertObjectToReference' is not defined
It is expected to be a closure like this:
{ object ->
    return [
        businessObjType: ...   // business object type/class (examples: 'invoice', 'supplier', 'purchase-order')
        businessObjId: ...     // business object unique id (examples: '123456789')
    ]
}
    `
""")
        }

        if (!history) {
            // used in all method/functions that read/write history records
            history = [add: { [:] }, search: { [] }]
        }
    }

    // sets object initial state
    // @param object - stateful object
    // @param user - user name who initiated event/transition (this info will be writted into object wortkflow history)
    // @param description - event/transition/object description (this info will be writted into object wortkflow history)
    // N!B!: history record fields 'from' is set to ''NULL' value and 'event' to '__START__' value
    def start(params) {
        def object = params.object
        def user = params.user
        String description = params.description

        Map schema = this.machineDefinition.schema
        def name = schema.name
        def initialState = schema.initialState

        Map objectConfiguration = this.machineDefinition.objectConfiguration
        String stateFieldName = objectConfiguration.stateFieldName

        object[stateFieldName] = initialState

        Map historyParams = [from: "NULL", to: initialState, event: "__START__"]
        historyParams += convertObjectToReference(object)
        historyParams += [user: user, description: description, workflowName: name]
        // add history record
        this.history.add(historyParams)

        return [object: object]
    }

    // returns current object state
    def currentState(params) {
        def object = params.object
        def stateFieldName = this.machineDefinition.objectConfiguration.stateFieldName
        return object[stateFieldName]
    }

    // returns a list of events (names) that are available at current object state
    // event is optional, it is required only if you search for transitions with the event
    def availableTransitions(params) {
        def object = params.object
        String event = params.event
        def request = params.request

        // calculate from state
        String from = currentState([object: object])
        // we can cut each transition to 'event' and 'auto', but not now (may be later)
        return machineDefinition.findAvailableTransitions([from: from, object: object, request: request, context: context, event: event])
    }

    // sends event
    // @param object - stateful object
    // @param event - name of the event to be send
    // @param user - user name who initiated event/transition (this info will be writted into object wortkflow history)
    // @param description - event/transition/object description (this info will be writted into object wortkflow history)
    // @param request - event request data
    Map sendEvent(params) {
        def object = params.object
        String event = params.event
        def user = params.user
        String description = params.description
        def request = params.request

        def schema = machineDefinition.schema
        def workflowName = schema.name
        def objectConfiguration = machineDefinition.objectConfiguration
        def stateFieldName = objectConfiguration.stateFieldName

        // calculate from state
        String from = currentState([object: object])

        Closure changeObjectState = { to -> object[stateFieldName] = to }

        Map searchParams = [from: from, event: event, object: object]
        searchParams += machineDefinition.prepareObjectAlias(object)
        searchParams += [request: request, context: context]

        List translations = this.machineDefinition.findAvailableTransitions(searchParams)

        if (translations.size() == 0) {
            throw new MachineSendEventException("Transition for 'from': '${from}' and 'event': '${event}' is not found", [object: object, from: from, event: event])
        } else if (translations.size() > 1) {
            log.warning("More than one transition is found for 'from': '${from}' and 'event': '${event}'")
        }

        // select first found transition and read its information
        // using node destruct syntax to get 1st element and destruct it to {to, actions} object
        String to = translations[0].to
        List actions = translations[0].actions

        def ok = canBeReleased([object: object, to: to, request: request])
        if (!ok) {
            throw new MachineSendEventException("Object cannot be released from '${from}' to '${to}'", [object: object, from: from, to: to, evnet: event])
        }

        // extracting actionDefinitions list from
        List actionDefinitions = actions.collect { action ->
            if (!machineDefinition.actions[action.name]) {
                throw new MachineSendEventException("Action '${action.name}' is specified in one of transitions but is not found/implemented!", [action: action.name, object: object, from: from, event: event, to: to])
            }
            return machineDefinition.actions[action.name]
        }

        Map implicitParams = [
                from   : from,
                to     : to,
                event  : event,
                object : object,
                request: request,
                context: context
        ] + machineDefinition.prepareObjectAlias(object)


        List actionExecutionResults = actionDefinitions.inject([], { actionExecutionResults, action ->
            int idx = actionDefinitions.indexOf(action)
            Map actionParams = this.machineDefinition.prepareParams(actions[idx].params, implicitParams)
            actionParams += [actionExecutionResults: actionExecutionResults]
            def actionResult = action(actionParams)

            return actionExecutionResults + [
                    [name: actions[idx].name, result: actionResult]
            ]
        })
        changeObjectState(to)

        Map historyParams = [
                from : from,
                to   : to,
                event: event,
        ]

        historyParams += convertObjectToReference(object)
        historyParams += [
                user        : user,
                description : description,
                workflowName: workflowName,
        ]

        this.history.add(historyParams)

        return [actionExecutionResults: actionExecutionResults, object: object]
    }

    /**
     * Checks if workflow is launched and not finished for a specified object
     */
    boolean isRunning(params) {
        def object = params.object
        return availableStates().indexOf(currentState([object: object])) != -1 && !isInFinalState([object: object])
    }

    /**
     * Return list of available workflow states
     */
    List availableStates() {
        return this.machineDefinition.getAvailableStates()
    }

    /**
     * Check if object in specified state
     *
     * @param params
     * @return returns true if object in specified state
     */
    boolean is(params) {
        def object = params.object
        String state = params.state
        return currentState([object: object]) == state
    }

    /**
     * returns true if object in one of final states specified in machine definition schema
     */
    boolean isInFinalState(params) {
        def object = params.object
        return this.machineDefinition.schema.finalStates.indexOf(this.currentState([object: object])) >= 0
    }

    /**
     * Check any transition can be released
     *
     * @param params
     * @return returns true if any transition can be released
     */
    boolean can(params) {
        String event = params.event
        def object = params.object
        List transitions = availableTransitions([object: object, event: event])
        return transitions.size() > 0 && !transitions.collect { transition -> canBeReleased([object: object, to: transition.to]) }.any {
            !it
        }
    }

    boolean cannot(params) {
        return !can(params)
    }

    /**
     * Is it allowed to release from current state?
     * @param{object} object - business object
     * @param{string} to - (optional) name of target state (can object release from state 'from' and transit to state 'to'?)
     * @param{object} request - (optional) request-specific data
     */
    boolean canBeReleased(params) {
        def object = params.object
        String to = params.to
        def request = params.request

        // calculate from state
        String from = currentState([object: object])

        def inspectionResults = machineDefinition.inspectReleaseConditions([from: from, to: to, object: object, request: request, context: this.context])
        // if no release conditions defined for this state then inspectReleaseConditions returns 'true'
        if (inspectionResults == true) {
            return true
        }
        // otherwise return 'false' when first 'false' result is met
        return !inspectionResults.any {
            it.result.any { it.result == false }
        }
    }

    /**
     * Provides access to business object history records within the workflow
     *
     * @param{Object} searchParameters search parameters
     * @param{string} searchParameters.object business object (process)
     * @param{string} searchParameters.user user name initiated event (examles: 'Friedrich Wilhelm Viktor Albert')
     * @param{Object} searchParameters.finishedOn time when transition was completed
     * @param{Date} searchParameters.finishedOn.gt means that finishedOn should be greater than passed date
     *  (example: Date("2018-03-05T21:00:00.000Z")
     * @param{Date} searchParameters.finishedOn.gte greater than or equal
     * @param{Date} searchParameters.finishedOn.lt lesser than
     * @param{Date} searchParameters.finishedOn.lte lesser than or equal
     * @param{Object} paging results paging parameters
     * @param{Object} sorting results searchong parameters
     *
     * @returns Promise that is resolved into an array which contains found history records
     *
     * History record is an object with the following structure:
     *{*   event,
     *   from,
     *   to,
     *   object,
     *   user,
     *   description,
     *   finishedOn
     *}
     */
    List getHistory(searchParams, pagingParams, sortingParams) {
        def object = searchParams.object
        def user = searchParams.user
        def finishedOn = searchParams.finishedOn

        def max = pagingParams?.max
        def offset = pagingParams?.offset

        def by = sortingParams?.by
        def order = sortingParams?.order

        return this.history.search([
                user        : user,
                finishedOn  : finishedOn,
                workflowName: machineDefinition.schema.name
        ] + convertObjectToReference(object), [max: max, offset: offset], [by: by, order: order]).collect { historyRecord ->
            String event = historyRecord.event
            String from = historyRecord.from
            String to = historyRecord.to
            def businessObjType = historyRecord.businessObjType
            def businessObjId = historyRecord.businessObjId
            String description = historyRecord.description

            return [event: event, from: from, to: to, object: [
                    businessObjType: businessObjType, businessObjId: businessObjId
            ], user      : historyRecord.user, description: description, finishedOn: historyRecord.finishedOn]
        }
    }
}
