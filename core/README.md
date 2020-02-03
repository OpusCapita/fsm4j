# FSM core

Machine and its definition.

## How To Use

### Include maven dependency

```
com.opuscapita.fsm:fsm-workflow-jvm-core:1.0.0-SNAPSHOT
```

### Use in code

```groovy
import com.opuscapita.fsm.Machine
import com.opuscapita.fsm.MachineDefinition
```

## Machine definition

Machine definition consists of:
- [schema](#schema)
- [actions](#action)
- conditions [guards](#guard-conditions)

### Example

```groovy
import com.opuscapita.fsm.MachineDefinition

def machineDefinition = new MachineDefinition([
  schema: [
    name: "invoice approval",
    initialState: "open",
    finalStates: ["approved"],
    states: [ // optional meta data for states
      [
        name: "open",
        description: "Open",
        // release guards specify if state can be released
        // if not specified then release is always allowed
        release: [
          [
            // if 'release[i].to' field is undefined then release guard is applied no matter what target state is
            guards: [ // just like in transitions
              [
                expression: "object.enabled === true"
              ],
              [
                name: "someFunction",
                params: [
                  [
                    name: "param1",
                    value: 100
                  ]
                ]
              ]
            ]
          ],
          [
            to: 'approved', // if 'to' is a string then the folling guards are checked only when trying to transit from 'open' to 'approved'
            guards: [...]
          ],
          [
            to: ['approved', 'awaitingConfirmation'], // the same as with string, but for a list of target states
            guards: [...]
          ]
        ]
      ],
      [ name: "approved", description: "Approved" ]
    ],
    transitions: [
      [
          from: "open",
          to: "approved",
          event: "approve",
          // guards define weither this transition is available or not
          guards: [
            [
              "name": "validate",
              "params": [
                [
                  "name": "param1",
                  "value": "value1"
                ],
                [
                  "name": "param2",
                  "value": "value2"
                ]
              ]
            },
            // guards can also be simple expressions
            [
              "expression": "invoice.netAmount < 10000"
            ]
          ],
          // actions are executed is sequence during transition; 'sendEvent' resolves only after all actions are finished
          actions: [
            [
              "name": "archive",
              "params": [
                [
                  "name": "param1",
                  "value": "value1"
                ],
                [
                  "name": "param2",
                  "value": "value2"
                ]
              ]
            ]
          ]
      ]
    ]
  ],
  actions: [
    archive: { params -> }
  ],
  conditions: [
    validate: { params -> },
    lastlyUpdatedMoreThan24hAgo: { params -> }
  ],
  objectConfiguration: [
    stateFieldName: "status",
    alias: "invoice",
    example: [
      "invoiceNo": "1111",
      "customerId": "wefwefewfew",
      "supplierId": "33333",
      "netAmount": 1000,
      "status": "reviewRequired"
    ],
    schema: [
      [
        title: "Invoice",
        type: "object",
        properties: [
          invoiceNo: [
            type: "string"
          ],
          customerId: [
            type: "string"
          ],
          supplierId: [
            type: "string"
          ],
          netAmount: [
            type: "number"
          ],
          status: [
            type: "string"
          ]
        ],
        required: ["invoiceNo"]
      ]
    ]
  ]
]);
```

### Schema

Defines machine transitions and initialization options. Could be presented as oriented graph, where each node represents state and directed edges are used to represent transition from one state to another.

#### Transitions

In schema you needs to define an array of available machine transitions. Typically a transition is triggered by an _event_ and happens between _from_ and _to_ states. Optionally each transition can have _actions_ and/or _guards_ (conditions).

#### Initial state

You can define the initial state by setting the _initialState_ property:

```groovy
import com.opuscapita.fsm.Machine
import com.opuscapita.fsm.MachineDefinition

def machineDefinition = new MachineDefinition([
  schema: [
    name: 'sprint'
    initialState: 'start'
    transitions: [
      [ from: 'start', event: 'run', to: 'finish' ]
    ]
  ]
]);

def machine = new Machine([ machineDefinition ]);
def result = machine.start([ object ])
assert result.status == "none"
```

if initial state is not specified, then 'none' will be used (TBD)

#### Final states

You can define the final states (one or many) by setting the _finalStates_ property:

```groovy
import com.opuscapita.fsm.MachineDefinition

def machineDefinition = new MachineDefinition([
  schema: [
    initialState: 'start',
    finalStates: ['finish'],
    transitions: [
      [from: 'start', event: 'run', to: 'finish']
    ]
  ]
]);
```

### Code (Actions and Conditions(guards))

- [Actions & conditions configuration and usage](../editor/src/components/Actions/Readme.md)
- [Ideas & thoughts](actionsAndConditions.md)

#### Action

Actions (action = function) are executed during transition (not while leaving/entering state). Action references specific function by name. Action implemented separately from schema. Each action accepts named arguments explicitly defined in transition and implicit arguments like _object_, _from_, _to_, etc. During transition machine executes each action in defined order. Each action gets _actionExecutionResults_ argument which serves as an accumulator from previously called actions, where each property is an action name and value is value returned by action.

#### Guard (conditions)

Guards are used to protect transitions. Guard works as 'if' condition.
Technically guard is defined the same way like as action, it is a function.
The difference is that it should always return boolean value (true or false).
Condition(function) result could be inverted if its property _negate_ is set to true.

Guards could be also _sync_ and _async_ functions. In case you want to implement async guard, pay additional attention
to the value resolved by a guard - it should be **only** boolean value. In case your guard rejects some value
(error or smth else) - it will be taken as an error and _findAvailableTransitions_ will be rejected with error.

Note: similar to [Spring State Machine Guards](http://docs.spring.io/spring-statemachine/docs/current/reference/htmlsingle/#configuring-guards)

## Stateful object as a process

Machine does not have own state, all the transitions are performed over object which state is changed by machine. Object is used by Machine as a mutable parameter passed to guards and actions.

```groovy
import com.opuscapita.fsm.MachineDefinition

def machineDefinition = new MachineDefinition([
  schema: [
    initialState: 'start'
    finalStates: ['finish'],
    transitions: [
      [ from: 'start', event: 'run', to: 'finish' ]
    ]
  ]
]);

def machine = new Machine([machineDefinition: machineDefinition])

def object = [ status: 'none' ]

def result = machine.start([ object: object ])
assert machine.currentState([ object: object ]) == "start"

machine.sendEvent({ object, event: 'run' })
assert machine.currentState([ object: object ]) == "finish"
```

## Object Workflow History

Machine configuration

```groovy
import com.opuscapita.fsm.MachineDefinition

def machine = new Machine([ history: history ]);
```

**history** is a DAO that provides a possibility to create and read object workflow history records. You can find its API (and DB specific implementation) [here](../history).

Machine writes history records for all object transitions within the workflow.
It happens when you start workflow
```groovy
machine.start([ object: object, user: user, description: description ])
```
or you send an event
```
machine.sendEvent([ object: object, event: event, user: user, description: description ])
```
In both cases, new history records are created.
Here
- **object** (required) - business object of the following structure
- **user** (required) - user identifier who initiated an event
- **description** (optional) - custom text that describes transition/object
All this info together is stored in workflow history.
Note: In case of **start** method call history fields **from** and **event** are filled with string value **NULL** (4 upper cases letters: N, U, L, L)

Machine provides possibility to get(search) history records via **getHistory** method:

Getting/searching specific workflow history. You can either search by:
- specific **object** history
- initiated by specific **user**
- additionally, you can restrict query using **finishedBy** to get history within a specific period of time.
```groovy
machine.getHistory(searchParameters, paging, sorting)
```
where
  - **searchParameters**
  ```groovy
  [
    object: object,
    user  : user,                    // example: 'john.miller'
    finishedOn: [
      gte: gte,                   // example: Date("2018-03-05T21:00:00.000Z")
      gt : gt,                    // (syntax like 'today', 'yesterday', 'week ago' could be introduced later, if required)
      lt : lt,
      lte: lte
    ]
  ]
  ```
  - **paging**:
  ```groovy
  [
    max: max,                      // example: '25', default value: 100
    offset: offset                    // example: '50', default value: 0
  ]
  ```
  - **sorting**:
  ```groovy
  {
   by: by,                        // example: 'user', default value: 'finishedOn',
                              // possible values: ['event', 'from', 'to', 'user', 'description', 'finishedOn']
   order: order                      // example: 'asc', default value: 'desc'
  }
  ```
  as a result you get a results into array of objects of the following structure:
  ```groovy
  [
    event: user,
    from: from,
    to: to,
    object: {
      businessObjectId: businessObjectId,
      businessObjectType: businessObjectType
    },
    user: user,
    description: description,
    finishedOn: finishedOn
  ]
  ```

**Note:** while writing workflow object history or searching history records by
object machine uses configured **convertObjectToReference** callback to convert
real business object into reference object that has the following structure
**[businessObjType, businessObjId]**

### Debugging schema

In case one needs to find out why particular transitions are available or unavailable there's a lower-level function `MachineDefinition.inspectTransitions`. According to provided input params it returns all analyzed transitions with their guards and results of their evaluation. This helps to determine why particular transition was (un)available in particular case.

In case one needs to inspect `release guards` defined for states, there's `MachineDefinition.inspectReleaseConditions` function which is similar to `MachineDefinition.inspectTransitions`.

For details about these functions see `MachineDefinition.groovy` source.

## Machine
### API

```groovy
def machineDefinition = new MachineDefinition([ schema: schema, conditions: conditions, actions: actions ])
// register workflow
def machine = new Machine([machineDefinition: machineDefinition, context: context]);

// start/initialize machine/workflow
machine.start([object: object])

// returns a list of available transitions: [event: event, from: from, to: to, request: request..], e.g. event
// request is used to pass parameters to guards for some dynamic calculation, e.g. when event availability depends
// on current user information as roles and etc.
machine.availableTransitions([ object: object ])
// returns a list of available automatic transitions: [event: event, from: from, to: to, ..], e.g. event
// if machine schema is adequate then there should be not more than 1 such transition
machine.availableAutomaticTransitions([:])

// send 'event' and pass addition 'request' data that is posted by user/app
// returns promise, in case of successful transition then function will be called
// with one parameter that is an JSON with the following structure:
// - object - object in new state (the same reference that is passed as parameter)
machine.sendEvent([object: object, event: event, request: request])

machine.currentState([object: object])     // gets current state
machine.is([object: object, state: state])         // is object in state
machine.isInFinalState([object: object])   // returns true iff object is in one of final states
machine.can([object: object, event: event ])       // whether event is available
machine.cannot([object: object, event: event])    // whether event is not available
machine.canBeReleased([object: object, to: to, request: request]) // weither object can be released from its current state; 'to' is an optional target state
```

## Contributors

| <img src="https://avatars.githubusercontent.com/u/24603787?v=3" width="100px;"/> | [**Alexey Sergeev**](https://github.com/asergeev-sc)     |
| :---: | :---: |
| <img src="https://avatars1.githubusercontent.com/u/31243790?v=3" width="100px;"/> | [**Egor Stambakio**](https://github.com/estambakio-sc) |
| <img src="https://avatars1.githubusercontent.com/u/24733803?v=4" width="100px;"/> | [**Dmitry Divin**](https://github.com/ddivin-sc) |

## License

**OpusCapita FSM Workflow** is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.
