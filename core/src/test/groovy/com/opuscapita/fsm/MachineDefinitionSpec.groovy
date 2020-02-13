package com.opuscapita.fsm

import spock.lang.Specification

/**
 * @author Dmitry Divin
 */
class MachineDefinitionSpec extends Specification {
    def "should available states"() {
        expect:
        new MachineDefinition(definition).availableStates == availableStates

        where:
        definition | availableStates
        [
                schema: [
                        states      : [
                                [name: "a"],
                                [name: "b"],
                                [name: "y"],
                                [name: "m"],
                                [name: "n"],
                                [name: "z"],
                        ],
                        transitions : [
                                [from: "a", to: "b"],
                                [from: "b", to: "x"],
                        ],
                        initialState: "a",
                        finalStates : ["x", "y", "z"],
                ]
        ]          | ["a", "b", "m", "n", "x", "y", "z"]
        //when transitions is empty
        [
                schema: [
                        states      : [
                                [name: "a"],
                                [name: "b"],
                                [name: "y"],
                                [name: "m"],
                                [name: "n"],
                                [name: "z"],
                        ],
                        transitions : [],
                        initialState: "a",
                        finalStates : ["x", "y", "z"],
                ]
        ]          | ["a", "b", "m", "n", "x", "y", "z"]
        //when states is empty
        [
                schema: [
                        states      : [],
                        transitions : [
                                [from: "a", to: "b"],
                                [from: "b", to: "x"],
                        ],
                        initialState: "a",
                        finalStates : ["x", "y", "z"],
                ]
        ]          | ["a", "b", "x", "y", "z"]
        //when transitions and states is empty
        [
                schema: [
                        states      : [],
                        transitions : [],
                        initialState: "a",
                        finalStates : ["x", "y", "z"],
                ]
        ]          | ["a", "x", "y", "z"]
    }

    def "should throws an error if 'from' is not specified"() {
        when:
        new MachineDefinition().findAvailableTransitions([:])
        then:
        thrown(AssertionError)
    }

    def "should returns empty list if transitions are not defined in machine schema"() {
        when:
        def transitions = new MachineDefinition().findAvailableTransitions([from: "anyState"])
        then:
        transitions.size() == 0
    }

    def "should finds appropriate transitions for specified 'from' and 'event'"() {
        expect:
        new MachineDefinition(
                [
                        schema: [
                                transitions: [
                                        [from: "a", to: "b", event: "a->b"],
                                        [from: "b", to: "c", event: "b->c"],
                                        [from: "a", to: "c", event: "a->c"],
                                ],
                        ]
                ]
        ).findAvailableTransitions(params).size() == sizeOfFoundItems

        where:
        params                     | sizeOfFoundItems

        [from: "a"]                | 2
        [from: "a", event: "a->b"] | 1
        [from: "a", event: "a->c"] | 1
        [from: "a", event: "a->a"] | 0
        [from: "b", event: "b->c"] | 1
        [from: "b", event: "b->a"] | 0
    }

    def "should 'guarded' transitions"() {
        given:
        def countOfTransitionItems
        MachineDefinition definition = new MachineDefinition(
                [
                        objectConfiguration: [alias: "invoice"],
                        schema             : [
                                transitions: [
                                        [from: "a", to: "b", event: "a->b", guards: [
                                                [name: "a-to-b", params: [[name: "one", value: 1], [name: "two", parmas: 2]]]
                                        ]],
                                        [from: "b", to: "c", event: "b->c", guards: [[name: "unavailable"]]],
                                        [from: "c", to: "d", event: "c->d", guards: [[name: "less-then-max", params: [[name: "max", value: 10]]]]],
                                        [from: "d", to: "e", event: "d->e", guards: [[name: "less-then-max", params: [[name: "max", value: 10]], negate: true]]],
                                        [from: "f", to: "g", event: "f->g", guards: [[expression: "object.enabled == true"]]],
                                        [from: "g", to: "h", event: "g->h", guards: [[expression: "invoice.enabled == true"]]],
                                        [from: "h", to: "i", event: "h->i", guards: [[expression: "not a valid expression"]]],
                                        [from: "i", to: "j", event: "i->j", guards: [[expression: "invoice.enabled == true", negate: true]]],
                                ]
                        ],
                        conditions         : [
                                "a-to-b"       : { param -> param.object.enabled },
                                "less-then-max": { param -> param.request.value < param.max },
                        ]
                ]
        )

        when:
        //guard forbids transition
        countOfTransitionItems = definition.findAvailableTransitions([from: "a", object: [enabled: false]]).size()
        then:
        countOfTransitionItems == 0

        when:
        //guard permits transition
        countOfTransitionItems = definition.findAvailableTransitions([from: "a", object: [enabled: true]]).size()
        then:
        countOfTransitionItems == 1

        when:
        //transition has reference to non declared guard(condition)
        definition.findAvailableTransitions([from: "b", object: [:]]).size()
        then:
        thrown(IllegalArgumentException)

        when:
        //check passing guard with passing request
        def transitions = definition.findAvailableTransitions([from: "c", object: [:], request: [value: 1]])
        then:
        transitions.size() == 1
        transitions[0].event == "c->d"
        transitions[0].from == "c"
        transitions[0].to == "d"

        when:
        //check rejecting guard with passing request
        countOfTransitionItems = definition.findAvailableTransitions([from: "c", object: [:], request: [value: 11]]).size()
        then:
        countOfTransitionItems == 0

        when:
        //negate guard permits transition
        countOfTransitionItems = definition.findAvailableTransitions([from: "d", object: [:], request: [value: 11]]).size()
        then:
        countOfTransitionItems == 1

        when:
        //expression guard evaluates properly
        countOfTransitionItems = definition.findAvailableTransitions([from: "f", object: [enabled: true]]).size()
        then:
        countOfTransitionItems == 1

        when:
        //expression guard has access to object alias
        countOfTransitionItems = definition.findAvailableTransitions([from: "g", object: [enabled: true]]).size()
        then:
        countOfTransitionItems == 1

        when:
        //expression guard forbids transition if expression throws an error
        definition.findAvailableTransitions([from: "h", object: []])
        then:
        thrown(MissingPropertyException)

        when:
        //expression guard doesn't get negated
        transitions = definition.findAvailableTransitions([from: "i", object: [enabled: true]])
        then:
        transitions.size() == 1
        transitions[0].event == "i->j"
    }

    def "should inspect release conditions"() {
        when:
        new MachineDefinition().inspectReleaseConditions([:])
        then:
        thrown(AssertionError)

        when:
        new MachineDefinition([:]).inspectReleaseConditions([:])
        then:
        thrown(AssertionError)

        when:
        def result = new MachineDefinition([:]).inspectReleaseConditions([from: "a"])

        then:
        result == true

        when:
        result = new MachineDefinition(
                [schema: [
                        transitions: [],
                        states     : [
                                [name: "a"]
                        ]
                ]]
        ).inspectReleaseConditions([from: "a"])

        then:
        result == true
    }

    def "should check when release conditions are defined"() {
        given:
        List result
        Map applyToAll = [
                // 'to' is not defined, this condition is relevant for all requests
                guards: [
                        [expression: "object.enabled"],
                        [name: "atLeast", params: [
                                [name: "fieldName", value: "total"],
                                [name: "limit", value: 10],
                        ]]
                ]
        ]
        List stateA = [
                applyToAll,
                [
                        to    : "b",
                        // relevant only for 'to' === 'b'
                        guards: [[expression: "object.total > 300"]]
                ],
                [
                        to    : ["d", "b", "c"],
                        // relevant for any of these states
                        guards: [[expression: "object.total > 100"]]
                ],
                [
                        to    : ["c"],
                        // relevant only for 'c'
                        guards: [[expression: "object.total > 500"]]
                ]
        ]
        MachineDefinition definition = new MachineDefinition([
                schema    : [
                        transitions: [],
                        states     : [
                                [name: "a", release: stateA]
                        ]
                ],
                conditions: [
                        atLeast: { param -> param.object[param.fieldName] >= param.limit }
                ]
        ])

        when:
        //for empty 'to' inspects only conditions with empty 'to'
        result = definition.inspectReleaseConditions([from: "a", object: [enabled: true, total: 150]])
        then:
        result.size() == 1
        result[0].condition == applyToAll

        when:
        //for defined 'to' inspects conditions both with same 'to', and if condition includes 'to', and with empty 'to'
        result = definition.inspectReleaseConditions([from: "a", to: "b", object: [enabled: true, total: 150]])
        then:
        result.size() == 3
        result[0].condition == stateA[0]
        result[1].condition == stateA[1]
        result[2].condition == stateA[2]
    }
}
