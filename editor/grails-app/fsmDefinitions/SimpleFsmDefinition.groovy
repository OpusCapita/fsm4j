import com.opuscapita.fsm.artefact.FsmDefinition

class SimpleFsmDefinition implements FsmDefinition {
    static final workflowName = "InvoiceApproval"

    static Map DEFAULT_SCHEMA = [
            'finalStates' : ['approved'],
            'initialState': 'inspectionRequired',
            'name'        : 'InvoiceApproval1',
            'states'      : [
                    ['name': 'inspectionRequired', 'description': 'Inspection Required'],
                    ['name': 'approvalRequired'],
                    ['name': 'inspClrRequired'],
                    ['name': 'inspectionRejected'],
                    ['name': 'approved'],
                    ['name': 'appClrRequired'],
                    ['name': 'approvalRejected']
            ],
            'transitions' : [
                    ['event'  : 'inspect', 'from': 'inspectionRequired', 'to': 'approvalRequired',
                     'actions': [
                             ['name': 'testAction', 'params': [
                                     ['name': 'dinnerMenu', 'value': ['Steak', 'Mashrooms']],
                                     ['name': 'fullName', 'value': 'John Smith']]
                             ],
                             ['name': 'sendMail', 'params': [
                                     ['name': 'fromAddress', 'value': 'support@client.com'],
                                     ['name': 'greeting', 'value': 'Mr. Twister']]
                             ],
                             ['name': 'updateProcessedBy', 'params': [
                                     ['name': 'someArray', 'value': ['one', 'two']],
                                     ['name': 'processedByFieldName', 'value': 'customerId']]
                             ]
                     ],
                     'guards' : [
                             ['expression': 'invoice["netAmount"] > 100'],
                             ['name': 'userHasRoles', 'params': [
                                     ['name': 'restrictedRoles', 'value': ['REV']]]]
                     ],
                    ],
                    ['event': 'automatic-inspect', 'from': 'inspectionRequired', 'to': 'approvalRequired'],
                    ['event': 'sendToClarification', 'from': 'inspectionRequired', 'to': 'inspClrRequired'],
                    ['event': 'clarifyForInspection', 'from': 'inspClrRequired', 'to': 'inspectionRequired'],
                    ['event': 'rejectInspection', 'from': 'inspectionRequired', 'to': 'inspectionRejected'],
                    ['event': 'rejectInspection', 'from': 'inspClrRequired', 'to': 'inspectionRejected'],
                    ['event': 'cancelRejection', 'from': 'inspectionRejected', 'to': 'inspectionRequired'],
                    ['event': 'cancelInspection', 'from': 'approvalRequired', 'to': 'inspectionRequired'],
                    ['event': 'rejectInspection', 'from': 'approvalRequired', 'to': 'inspectionRejected'],
                    ['event': 'approve', 'from': 'approvalRequired', 'to': 'approved'],
                    ['event': 'sendToClarification', 'from': 'approvalRequired', 'to': 'appClrRequired'],
                    ['event': 'rejectApproval', 'from': 'approvalRequired', 'to': 'approvalRejected'],
                    ['event': 'clarifyForApproval', 'from': 'appClrRequired', 'to': 'approvalRequired'],
                    ['event': 'rejectApproval', 'from': 'appClrRequired', 'to': 'approvalRejected'],
                    ['event': 'cancelApproval', 'from': 'approved', 'to': 'approvalRequired'],
                    ['event': 'rejectApproval', 'from': 'approved', 'to': 'approvalRejected'],
                    ['event': 'sendToClarification', 'from': 'approved', 'to': 'appClrRequired'],
                    ['event': 'cancelRejection', 'from': 'approvalRejected', 'to': 'approvalRequired']
            ]
    ]


    @Override
    Map getSchema() {
        return DEFAULT_SCHEMA
    }

    @Override
    void setSchema(Map updatedSchema) {
    }

    @Override
    Map getObjectConfiguration() {
        [
                'alias'         : 'invoice',
                'example'       : [
                        'currencyId' : 'EUR',
                        'customerId' : 'wefwefewfew',
                        'grossAmount': 1200,
                        'invoiceNo'  : '1111',
                        'netAmount'  : 1000,
                        'status'     : 'inspectionRequired',
                        'supplierId' : '33333',
                        'vatAmount'  : 200
                ],
                'schema'        : [
                        'properties': [
                                'currencyId': ['enum': ['EUR', 'USD', 'RUB', 'CHF', 'HKD', 'JPY', 'AUD', 'CAD'], 'type': 'string'],
                                'customerId': ['type': 'string'], 'grossAmount': ['minimum': 0, 'type': 'integer'],
                                'invoiceNo' : ['type': 'string'],
                                'netAmount' : ['minimum': 0, 'type': 'integer'],
                                'status'    : ['enum': ['inspectionRequired', 'approvalRequired', 'inspClrRequired', 'inspectionRejected', 'approved', 'appClrRequired', 'approvalRejected'], 'type': 'string'],
                                'supplierId': ['type': 'string'], 'vatAmount': ['minimum': 0, 'type': 'integer']
                        ],
                        'required'  : ['invoiceNo', 'customerId', 'supplierId', 'netAmount', 'grossAmount', 'vatAmount', 'currencyId', 'status'],
                        'type'      : 'object'
                ],
                'stateFieldName': 'status'
        ]
    }

    @Override
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
                        ]
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
                        ]
                ],
                'updateProcessedBy': [
                        'paramsSchema': [
                                'properties': ['processedByFieldName': ['type': 'string']],
                                'required'  : ['processedByFieldName'],
                                'type'      : 'object'
                        ]
                ]
        ]
    }

    @Override
    Map getConditions() {
        [
                'userHasRoles': [
                        'paramsSchema': [
                                'properties': [
                                        'restrictedRoles': ['items': ['enum': ['REV', 'BOSS'], 'type': 'string'], 'type': 'array']],
                                'type'      : 'object'
                        ]
                ]
        ]
    }
}