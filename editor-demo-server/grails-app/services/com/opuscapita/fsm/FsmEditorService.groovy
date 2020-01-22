package com.opuscapita.fsm

import groovy.json.JsonSlurper

/**
 * Demo FSM editor
 *
 * @author Dmitry Divin
 */
class FsmEditorService {
    def workflowTransitionHistoryService

    private Map internalSchema

    private getJSONFromClassPath(String resourceName) {
        new JsonSlurper().parse(getClass().classLoader.getResourceAsStream(resourceName))
    }

    Map getSchema() {
        if (internalSchema == null) {
            internalSchema = getJSONFromClassPath("com/opuscapita/fsm/default-schema.json")
        }
        return internalSchema
    }

    void setSchema(Map updatedSchema) {
        internalSchema = updatedSchema
    }

    Map getObjectConfiguration() {
        getJSONFromClassPath("com/opuscapita/fsm/objectConfiguration.json")
    }

    Map getConditions() {
        [
                'userHasRoles': [
                        'paramsSchema': [
                                'properties': [
                                        'restrictedRoles': ['items': ['enum': ['REV', 'BOSS'], 'type': 'string'], 'type': 'array']],
                                'type'      : 'object'
                        ], expression : "return true"
                ]
        ]
    }

    Map getActions() {
        [
                'sendMail'         : [
                        'paramsSchema': [
                                'properties': [
                                        'expiryDate' : ['format': 'date', 'type': 'string'],
                                        'fromAddress': ['type': 'string'],
                                        'greeting'   : ['type': 'string'],
                                        'interest'   : ['type': 'number'],
                                        'language'   : ['enum': ['en', 'de', 'fi', 'ru', 'sv', 'no'], 'type': 'string'],
                                        'maxRetries' : ['type': 'integer'],
                                        'priority'   : ['enum': [0, 1, 2, 3, 4, 5], 'type': 'integer'],
                                        'sendCopy'   : ['type': 'boolean']],
                                'required'  : ['fromAddress', 'greeting'],
                                'type'      : 'object'
                        ], expression : "return true"
                ],
                'testAction'       : [
                        'paramsSchema': [
                                'properties': [
                                        'adult'                 : ['type': 'boolean'],
                                        'age'                   : ['type': 'integer'],
                                        'bankAccountBalance'    : ['type': 'number'],
                                        'children'              : ['enum': [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 'type': 'integer'],
                                        'confidenceRate'        : ['enum': [15.75, 44.55, 66.7, 99999.9], 'type': 'number'],
                                        'dateOfBirth'           : ['format': 'date', 'type': 'string'],
                                        'dinnerMenu'            : ['items': ['enum': ['Steak', 'Vegetables', 'Mashrooms', 'Beer'], 'type': 'string'], 'type': 'array'],
                                        'favoriteColor'         : ['enum': ['red', 'green', 'blue', 'yellow', 'I\'m achromate'], 'type': 'string'],
                                        'fullName'              : ['type': 'string', 'uiComponent': 'fullName'],
                                        'importantDates'        : ['items': ['format': 'date', 'type': 'string'], 'type': 'array'],
                                        'monthlyInterestHistory': ['items': ['type': 'number'], 'type': 'array'],
                                        'nextLotteryNumbers'    : ['items': ['type': 'integer'], 'type': 'array'],
                                        'nickname'              : ['type': 'string'],
                                        'todoList'              : ['items': ['type': 'string'], 'type': 'array']],
                                'type'      : 'object'
                        ], expression : "return true"
                ],
                'updateProcessedBy': [
                        'paramsSchema': [
                                'properties': ['processedByFieldName': ['type': 'string']],
                                'required'  : ['processedByFieldName'],
                                'type'      : 'object'
                        ], expression : "return true"
                ]
        ]
    }

    Machine getMachine() {
        MachineDefinition machineDefinition = new MachineDefinition([
                schema             : schema,
                objectConfiguration: objectConfiguration,
                conditions         : [
                        userHasRoles: { args ->
                            log.info("Condition [userHasRoles] with args [${args}]")
                            return true
                        }
                ],
                actions            : [
                        sendMail         : { args ->
                            log.info("Action [sendMail] with args [${args}]")
                        },
                        testAction       : { args ->
                            log.info("Action [testAction] with args [${args}]")
                        },
                        updateProcessedBy: { args ->
                            log.info("Action [updateProcessedBy] with args [${args}]")
                        },
                ]
        ])

        Machine machine = new Machine([
                machineDefinition       : machineDefinition,
                history                 : workflowTransitionHistoryService,
                convertObjectToReference: { object ->
                    return [
                            businessObjType: "invoice",
                            businessObjId  : "IN01"
                    ]
                }
        ])

        return machine
    }
}
