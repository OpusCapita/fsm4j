package com.opuscapita.fsm


import static com.opuscapita.fsm.ParametersTypeDefinition.createTypeDefinition

/**
 * Machine Definition
 *
 * @author Alexey Sergeev
 * @author Dmitry Divin
 */
class MachineDefinition {
    Map schema
    //condition is an object, where each property name is condition name and
    //value is condition implentation (function)
    Map conditions
    //actions is an object, where each property is implemented action name and
    //value is action(function) itself
    Map actions
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
    Map objectConfiguration

    MachineDefinition(Map params = [:]) {
        createTypeDefinition()
                .withType("schema.states", List)
                .withType("schema.finalStates", List)
                .withType("schema.transitions", List)
                .withType("conditions", Map)
                .withType("actions", Map)
                .withType("objectConfiguration", Map)
                .validate(params)

        Map schema = params.schema ?: [:]
        this.schema = [finalStates: []] + schema

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
     * @param conditions
     * @param implicitParams
     *
     * @return result as boolean
     */
    private List<Map> inspectConditions(Map params = [:]) {
        createTypeDefinition()
                .isRequired("conditions")
                .withType("conditions", Collection)
                .withType("implicitParams", Map)
                .withType("classLoader", ClassLoader)
                .isRequired("implicitParams")
                .validate(params)

        Map implicitParams = params.implicitParams
        ClassLoader classLoader = params.classLoader
        // collecting conditions that belong to current transition
        List preparedConditions = params.conditions.collect { Map condition ->
            if (condition.expression) {
                return condition
            }
            if (!conditions[condition.name]) {
                throw new IllegalArgumentException("Constraint '${condition.name}' is specified in one of transitions but corresponding condition is not found/implemented!")
            }
            return conditions[condition.name]
        }


        List result = []
        for (int i = 0; i < preparedConditions.size(); i++) {
            boolean isExpression = false
            def condition = preparedConditions[i]
            def res
            if (condition instanceof Closure) {
                res = condition(
                        prepareParams(params.conditions[i].params, implicitParams, classLoader)
                ) as Boolean
            } else {
                isExpression = true
                String expression = condition.expression
                res = evaluateExpression(expression, implicitParams, classLoader) as Boolean
            }
            // `negate` property is applied only to function invocations
            result << [condition: params.conditions[i], result: params.conditions[i].negate && !isExpression ? !res : res]
        }

        return result
    }

    static evaluateExpression(String expression, Map implicitParams, ClassLoader classLoader) {
        GroovyShell groovyShell = new GroovyShell(classLoader, new Binding(implicitParams))
        return groovyShell.evaluate(expression)
    }

    // evaluate explicit params and combine with implicit params
    // this function is not made static because it is used in Machine.groovy via instance accessor
    Map prepareParams(Collection explicitParams, Map implicitParams, ClassLoader classLoader) {
        return explicitParams.inject([:], { Map result, Map param ->
            String name = param.name
            def value = param.value
            String expression = param.expression

            result[name] = expression ? evaluateExpression(expression, implicitParams, classLoader) : value
            result
        }) + implicitParams
    }

    List findAvailableTransitions(Map params = [:]) {
        createTypeDefinition().isRequired("from").validate(params)
        String from = params.from
        String event = params.event
        def object = params.object
        def request = params.request
        def context = params.context

        return inspectTransitions([from: from, event: event, object: object, request: request, context: context]).findAll { Map res ->
            Map result = res.result
            Map transition = res.transition
            return transition.guards ? result.guards.every({ it.result }) : true
        }.collect { it.transition }
    }

    /**
     * inspectTransitions is a generic function which returns transitions with evaluated conditions
     */
    List inspectTransitions(Map params = [:]) {
        createTypeDefinition().isRequired("from").validate(params)

        String from = params.from
        String event = params.event
        def object = params.object
        def request = params.request
        def context = params.context

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
     * @param params.from - state to be inspected
     * @param params.to (optional) - 'to' state in release guards
     */
    def inspectReleaseConditions(Map params = [:]) {
        createTypeDefinition().isRequired("from").validate(params)

        String from = params.from
        String to = params.to
        def object = params.object
        def request = params.request
        def context = params.context

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
        if (this.schema.states && this.schema.states.size() > 0) {
            result += this.schema.states.collect { state -> state.name }
        }

        if (this.schema.transitions && this.schema.transitions.size() > 0) {
            result += this.schema.transitions.inject([], { acc, next -> acc + [next.from, next.to] })
        }

        return result.unique().sort()
    }
}
