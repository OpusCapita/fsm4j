package com.opuscapita.fsm

import spock.lang.Specification

/**
 * Specification tests for Machine
 *
 * @author Dmitry Divin
 */
class MachineSpec extends Specification {
    private Machine createMachine(Map params = [:]) {
        return new Machine([
                machineDefinition: new MachineDefinition([
                        schema: [
                                transitions: [
                                        [from: "opened", to: "closed", event: "close"],
                                        [from: "closed", to: "opened", event: "open"],
                                ],
                                states     : (params.states ?: [])
                        ]
                ])
        ])
    }

    def "should machine: can returns true"() {
        expect:
        createMachine().can([object: [status: "opened"], event: "close"])
        !createMachine().cannot([object: [status: "opened"], event: "close"])
    }

    def "should machine can returns false"() {
        expect:
        !createMachine().can([object: [status: "opened"], event: "open"])
        createMachine().cannot([object: [status: "opened"], event: "open"])
    }

    def "should machine can returns false is release guard denies transition"() {
        expect:
        !createMachine([
                states: [
                        [
                                name   : "opened",
                                release: [
                                        [guards: [[expression: "object.enabled"]]]
                                ]
                        ]
                ]
        ]).can([object: [status: "opened", enabled: false], event: "close"])
    }

    def "should machine can returns true is release guard allows transition"() {
        expect:
        createMachine([
                states: [
                        [
                                name   : "opened",
                                release: [
                                        [guards: [[expression: "object.enabled"]]]
                                ]
                        ]
                ]
        ]).can([object: [status: "opened", enabled: true], event: "close"])
    }

    def "should machine can't returns true"() {
        expect:
        createMachine().cannot([object: [status: "opened"], event: "open"])
    }

    def "should machine can't returns false"() {
        expect:
        !createMachine().cannot([object: [status: "opened"], event: "close"])
    }

    def "should machine can release if states are not defined in schema"() {
        expect:
        new Machine([machineDefinition: new MachineDefinition()]).canBeReleased([object: [status: "a"]])
    }

    def "should machine can release if no release conditions defined for current state"() {
        expect:
        new Machine([machineDefinition: new MachineDefinition(
                schema: [
                        states: [
                                [name: "a"]
                        ]
                ]
        )]).canBeReleased([object: [status: "a"]])
    }

    def "should machine if release conditions are defined"() {
        given:
        Machine machine = new Machine([machineDefinition: new MachineDefinition(
                schema: [
                        transitions: [],
                        states     : [
                                [
                                        name   : "a",
                                        release: [
                                                [
                                                        guards: [
                                                                [expression: "object.enabled"],
                                                                [name: "atLeast", params: [[name: "fieldName", value: "total"], [name: "limit", value: 10]]]
                                                        ]
                                                ],
                                                [
                                                        to    : "b",
                                                        guards: [
                                                                // relevant only for 'to' === 'b'
                                                                [expression: "object.total > 300"]
                                                        ]
                                                ],
                                                [
                                                        to    : ["d", "b", "c"],
                                                        guards: [
                                                                // relevant for any of these states
                                                                [expression: "object.total > 100"]
                                                        ]
                                                ],
                                                [
                                                        to    : "c",
                                                        guards: [
                                                                // irrelevant for 'a' -> (undefined | 'b') requests
                                                                [expression: "object.irrelevant == 'gonnafail'"]
                                                        ]
                                                ]
                                        ]
                                ]
                        ],
                ],
                conditions: [
                        atLeast: { param ->
                            return param.object[param.fieldName] >= param.limit
                        }
                ]
        )])
        expect:
        machine.canBeReleased([object: [status: "a", enabled: true, total: 150]])
        !machine.canBeReleased([object: [status: "a", enabled: false, total: 150]])
        machine.canBeReleased([object: [status: "a", enabled: true, total: 301], to: "b"])
        !machine.canBeReleased([object: [status: "a", enabled: true, total: 299], to: "b"])
    }

    def "should machine current state"() {
        expect:
        new Machine([machineDefinition: new MachineDefinition()]).currentState([object: [status: "new"]]) == "new"
    }

    def "should get history"() {
        given:
        Closure convertObjectToReference = { object ->
            [
                    businessObjId  : 'Usain Bolt',
                    businessObjType: 'sprinter'
            ]
        }
        List passedSearchParameters = []
        List passedPaging = []
        List passedSorting = []

        Map object = [nick: "flash"]
        List foundHistoryRecords = [
                [
                        from        : "start",
                        to          : "finish",
                        event       : "gong-blow",
                        finishedOn  : new Date(),
                        user        : "start gate official",
                        description : "stadium streams: 'run baby, run!'",
                        workflowName: "sprint"
                ] + convertObjectToReference(object)
        ]
        Machine machine = new Machine([
                machineDefinition       : new MachineDefinition(
                        [schema: [name: "sprint"]]
                ),
                history                 : [
                        search: { searchParams, paging, sorting ->
                            passedSearchParameters << searchParams
                            passedPaging << paging
                            passedSorting << sorting

                            return foundHistoryRecords
                        }
                ],
                convertObjectToReference: convertObjectToReference
        ])

        when:
        Map searchParams = [object: object, user: "start gate official", finishedOn: null]
        Map paging = [max: 30, offset: 99]
        Map sorting = [by: "finishedOn", order: "desc"]

        List res = machine.getHistory(searchParams, paging, sorting)

        then:
        passedSearchParameters.size() == 1
        passedPaging.size() == 1
        passedSorting.size() == 1
        passedSearchParameters[0] == ([user: searchParams.user, finishedOn: searchParams.finishedOn] + [workflowName: "sprint"] + convertObjectToReference(object))
        passedPaging[0] == paging
        passedSorting[0] == sorting
        res.size() == 1
        res[0].object == convertObjectToReference(object)
        res[0].from == foundHistoryRecords[0].from
        res[0].to == foundHistoryRecords[0].to
        res[0].event == foundHistoryRecords[0].event
        res[0].finishedOn == foundHistoryRecords[0].finishedOn
        res[0].user == foundHistoryRecords[0].user
        res[0].description == foundHistoryRecords[0].description
    }

    def "should machine is returns correct value"() {
        given:
        Machine machine = new Machine(
                [
                        machineDefinition: new MachineDefinition()
                ]
        )
        expect:
        machine.is([object: [status: "new"], state: "new"])
        !machine.is([object: [status: "new"], state: "incorrect"])
    }

    def "should machine isInFinalState returns correct value"() {
        given:
        Machine machine = new Machine(
                [
                        machineDefinition: new MachineDefinition([
                                schema: [
                                        finalStates: ["x", "y", "z"]
                                ]
                        ])
                ]
        )
        expect:
        machine.isInFinalState([object: [status: "x"]])
        !machine.isInFinalState([object: [status: "a"]])
    }

    def "should machine isRunning returns correct values"() {
        given:
        Machine machine = new Machine(
                [
                        machineDefinition: new MachineDefinition([
                                schema: [
                                        finalStates: ["finished"],
                                        transitions: [
                                                [from: "started", to: "finished"]
                                        ]
                                ]
                        ])
                ]
        )
        expect:
        !machine.isRunning([object: [status: "none"]])
        machine.isRunning([object: [status: "started"]])
        !machine.isRunning([object: [status: "finished"]])
    }

    def "should available states"() {
        when:
        Machine machine = new Machine(
                [
                        machineDefinition: new MachineDefinition([
                                schema: [
                                        initialState: "started",
                                        finalStates: ["finished"],
                                        transitions: [
                                                [from: "started", to: "finished"]
                                        ]
                                ]
                        ])
                ]
        )

        then:
        machine.availableStates() == ["finished", "started"]
    }

    def "should machine start"() {
        given:
        Closure convertObjectToReference = { object ->
            [
                    businessObjId  : "tesla",
                    businessObjType: "car"
            ]
        }
        Closure createMachine = { history ->
            return new Machine([
                    machineDefinition       : new MachineDefinition([
                            schema: [
                                    name        : "verification",
                                    initialState: "started"
                            ]
                    ]),
                    convertObjectToReference: convertObjectToReference,
                    history                 : history
            ])
        }

        when:
        def machine = createMachine()
        def result = machine.start([object: [status: "none"]])

        then:
        result.object.status == "started"

        when:
        List passedParams = []
        machine = createMachine([add: { params -> passedParams << params }])
        result = machine.start([object: [status: "none"], user: "johnny", description: "getoff!"])

        then:
        passedParams.size() == 1
        passedParams[0] == [from: "NULL", to: "started", event: "__START__", user: "johnny", description: "getoff!", workflowName: "verification"] + convertObjectToReference([staus: "none"])
        result.object.status == "started"
    }

    def "should machine send event"() {
        given:
        def machine
        def result
        Closure convertObjectToReference = { object ->
            [
                    businessObjId  : "tesla",
                    businessObjType: "car"
            ]
        }
        Closure createMachine = { params ->
            return new Machine([
                    machineDefinition       : new MachineDefinition([
                            objectConfiguration: params?.objectAlias ? [alias: params.objectAlias] : null,
                            schema             : [
                                    states      : params?.states,
                                    initialState: "started",
                                    transitions : [
                                            [from: "started", to: "first-stop", event: "move"],
                                            [from: "first-stop", to: "second-stop", event: "move (action is not defined)", actions: [[name: "nonExistingAction"]]],
                                            [from: "first-stop", to: "second-stop", event: "move (action is defined)", actions: [[name: "sendEmail", params: [[name: "first", value: 1], [name: "second", value: "2"]]]]],
                                            [from: "first-stop", to: "second-stop", event: "doAsync", actions: [
                                                    [name: "dAsync0", params: [[name: "one", value: 1], [name: "two", value: 2]]],
                                                    [name: "dAsync1", params: [[name: "three", value: 3], [name: "four", value: 4]]],
                                            ]],
                                    ]
                            ],
                            actions            : params?.actions
                    ]),
                    convertObjectToReference: convertObjectToReference,
                    history                 : params?.history
            ])
        }

        when:
        machine = createMachine()
        //sends "move" event that moves object to the next state correctly
        result = machine.sendEvent([object: [status: "started"], event: "move"])

        then:
        result.object.status == "first-stop"

        when:
        //sends "step-back" event that does not exist
        machine.sendEvent([object: [status: "first-stop"], event: "step-back"])

        then:
        //Transition for 'from': 'first-stop' and 'event': 'step-back' is not found
        thrown(MachineSendEventException)

        when:
        //sends "move (action is not defined)" that requires action execution, but action is not defined/implemented
        machine.sendEvent([object: [status: "first-stop"], event: "move (action is not defined)"])

        then:
        //event/transition 'move (action is not defined)' should fail as sepcified action(s) is not defined
        thrown(MachineSendEventException)

        when:
        machine = createMachine([
                actions: [
                        sendEmail: {
                            //return some result
                            "awesomeEmail"
                        }
                ]
        ])
        //sends "move (action is defined)" that requires predefined action execution
        result = machine.sendEvent([object: [status: "first-stop"], event: "move (action is defined)"])

        then:
        result.object.status == "second-stop"
        result.actionExecutionResults.size() == 1
        result.actionExecutionResults[0].name == "sendEmail"
        result.actionExecutionResults[0].result == "awesomeEmail"

        when:
        //action has access to configured object alias
        machine = createMachine([
                actions    : [
                        sendEmail: { params ->
                            //return some result
                            [car: params.car]
                        }
                ],
                objectAlias: "car"
        ])
        //action has access to configured object alias
        result = machine.sendEvent([object: [status: "first-stop"], event: "move (action is defined)"])

        then:
        result.object.status == "second-stop"
        result.actionExecutionResults.size() == 1
        result.actionExecutionResults[0].name == "sendEmail"
        result.actionExecutionResults[0].result == [car: [status: "second-stop"]]

        when:
        machine = createMachine([
                states: [
                        [name   : "started",
                         release: [
                                 [to: "first-stop", guards: [[expression: "object.enabled"]]]
                         ]]
                ]
        ])
        //rejects if canBeReleased returns false
        machine.sendEvent([object: [status: "started", enabled: false], event: "move"])

        then:
        thrown(MachineSendEventException)
    }
}
