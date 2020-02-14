package com.opuscapita.fsm

/**
 * Machine Definition
 *
 * @author Alexey Sergeev
 * @author Dmitry Divin
 */
class MachineDefinition {
    private def schema
    //condition is an object, where each property name is condition name and
    //value is condition implementation (function)
    private def conditions
    //actions is an object, where each property is implemented action name and
    //value is action(function) itself
    private def actions
    /**
     * objectConfiguration is required by machine/engine and editor:
     *{*   stateFieldName,       // (String) object property that holds current object state
     *   schema,               // (Object) object JSON schema, will be used by the
     *                         //   editor to build expressions using object structure information
     *   alias,                // (String) object alias that will be used in action/guard
     *                         //   calls an implicit variable that is a reference to an object.
     *                         //   For example for invoice approval object alias could be 'invoice',
     *                         //   e.g. in guard expression user could type invoice.total < 1
     *                         //   instead of object.total < 1
     *   example              // (Object) object example that is used in editor
     *}*/
    private def objectConfiguration
    private ClassLoader classLoader

    /**
     * The constructor
     *
     * @param schema - (optional) Map of schema definition
     * @param schema.states - (optional) List of states
     * @param schema.finalStates - (optional) List of final states
     * @param schema.transitions - (optional) List of transitions
     * @param conditions - (optional) Map of conditions definitions
     * @param actions - (optional) Map of actions definitions
     * @param objectConfiguration - (optional) Map of object configuration
     * @param classLoader - (optional) ClassLoader usage, need for evaluate Groovy expressions
     */
    MachineDefinition(params = [:]) {
        Map schema = params.schema ?: [:]
        this.schema = [states: [], finalStates: [], transitions: []] + schema
        this.classLoader = params.classLoader

        this.conditions = params.conditions ?: [:]
        this.actions = params.actions ?: [:]
        this.objectConfiguration = [
                stateFieldName: defaultObjectStateFieldName
        ] + (params.objectConfiguration ?: [:])
    }

    static getDefaultObjectStateFieldName() {
        return "status"
    }

    /**
     * is a generic function which returns passed conditions with results of evaluation
     *
     * @param conditions - (required) inbound conditions of type Collection
     * @param implicitParams - (required) Map of implicit parameters
     *
     * @return result as boolean
     */
    private List<Map> inspectConditions(params) {
        //validate inbound conditions
        for (condition in params.conditions) {
            if (!condition.expression && !this.conditions[condition.name]) {
                throw new IllegalArgumentException("Constraint '${condition.name}' is specified in one of transitions but corresponding condition is not found/implemented!")
            }
        }
        Map implicitParams = params.implicitParams
        // collecting conditions that belong to current transition
        return params.conditions.collect { Map condition ->
            if (condition.expression) {
                boolean result = evaluateExpression(condition.expression, implicitParams) as Boolean
                return [condition: condition, result: result]
            } else {
                String name = condition.name
                def preparedCondition = conditions[name]
                boolean result = preparedCondition(prepareParams(condition.params, implicitParams)) as Boolean
                return [condition: condition, result: condition.negate ? !result : result]
            }
        }
    }

    private def evaluateExpression(String expression, implicitParams) {
        GroovyShell groovyShell = new GroovyShell(classLoader, new Binding(implicitParams))
        return groovyShell.evaluate(expression)
    }

    // evaluate explicit params and combine with implicit params
    // this function is not made static because it is used in Machine.groovy via instance accessor
    Map prepareParams(explicitParams, implicitParams) {
        return explicitParams.inject([:], { Map result, Map param ->
            String name = param.name
            def value = param.value
            String expression = param.expression

            result[name] = expression ? evaluateExpression(value, implicitParams) : value
            result
        }) + implicitParams
    }

    /**
     * @param from - (required) transition from
     *
     * @return list of available transitions
     */
    List findAvailableTransitions(params) {
        String from = params.from
        String event = params.event
        def object = params.object
        def request = params.request
        def context = params.context

        assert from != null, "Parameter [from] is mandatory"

        return inspectTransitions([from: from, event: event, object: object, request: request, context: context]).findAll { Map res ->
            Map result = res.result
            Map transition = res.transition
            return transition.guards ? result.guards.every({ it.result }) : true
        }.collect { it.transition }
    }

    /**
     * inspectTransitions is a generic function which returns transitions with evaluated conditions
     *
     * @param from - (required) transition from
     */
    List inspectTransitions(params) {
        String from = params.from
        String event = params.event
        def object = params.object
        def request = params.request
        def context = params.context

        assert from != null, "Parameter [from] is mandatory"

        List transitions = this.schema.transitions
        if (transitions) {
            Closure checkFrom = { transition -> transition.from == from }
            Closure checkEvent = { transition ->
                // if event is not specified then event does not matter for search
                return !event ? true : transition.event == event
            }

            return transitions.findAll { checkEvent(it) && checkFrom(it) }.collect { transition ->
                String to = transition.to
                String _event = transition.event
                List guards = transition.guards

                Map implicitParams = [
                        from   : from,
                        to     : to,
                        event  : _event,
                        object : object,
                        request: request,
                        context: context
                ] + prepareObjectAlias(object)

                Map result = [:]
                if (guards) {
                    result.guards = inspectConditions([conditions: guards, implicitParams: implicitParams])
                }

                return [transition: transition, result: result]
            }
        } else {
            // if not transitions, then return empty list
            return []
        }
    }

    // if objectConfiguration.alias is set up then
    // Map {<alias>: object} is returned otherwise empty Map
    Map prepareObjectAlias(def object) {
        if (this.objectConfiguration.alias) {
            return [(this.objectConfiguration.alias): object]
        }
        return [:]
    }

    /**
     * inspectReleaseConditions inspects 'release' guards defined in schema.states['mystate'].release (optional)
     *
     * Release is permitted if not stated otherwise.
     *
     * @param from - (required) state to be inspected
     * @param to (optional) - 'to' state in release guards
     */
    def inspectReleaseConditions(params) {
        String from = params.from
        String to = params.to
        def object = params.object
        def request = params.request
        def context = params.context

        assert from != null, "Parameter [from] is mandatory"

        // get state definition (if exists) from schema
        def state = this.schema.states?.find { it.name == from }

        // if no release guards defined for this state then return 'true'
        if (!state || !state.release) {
            return true
        }

        Map implicitParams = [
                from   : from,
                to     : to,
                object : object,
                request: request,
                context: context
        ] + prepareObjectAlias(object)

        /**
         * Get release conditions which are relevant for current request.
         * if condition.to === undefined then it works for any 'to'
         * if condition.to === 'some_name' then it works only for 'to' === 'some_name'
         * if condition.to === ['one', 'two'] then it works for 'to' === 'one' and 'to' === 'two'
         */
        return state.release.findAll { releaseCondition ->
            if (to == null) {
                return releaseCondition.to == null
                // if 'to' is not defined in release condition then it's relevant for any 'to' in request
            } else if (releaseCondition.to == null) {
                return true
            } else if (releaseCondition.to instanceof Collection) {
                return releaseCondition.to.contains(to)
            } else {
                return releaseCondition.to == to
            }
        }.collect { condition ->
            def inspectedReleaseConditions = inspectConditions([conditions: condition.guards, implicitParams: implicitParams])
            return [condition: condition, result: inspectedReleaseConditions]
        }
    }

    /**
     * Returns a list of all states that are defined in schema
     */
    List getAvailableStates() {
        List result = [this.schema.initialState] + this.schema.finalStates
        if (this.schema.states) {
            result += this.schema.states.collect { state -> state.name }
        }

        if (this.schema.transitions) {
            result += this.schema.transitions.inject([], { acc, next -> acc + [next.from, next.to] })
        }

        return result.unique().sort()
    }
}
