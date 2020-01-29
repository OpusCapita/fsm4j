### Machine Schema

```groovy
def schema = [
  "name": "invoice approval",             // workflow name
  "initialState": "open",                 // init state
  "finalStates": [                        // final states
    "authorized",
    "canceled"
  ],
  "objectConfiguration": [
    "stateFieldName": "status"            // object field where state is stored
  ],
  "transitions": [
    [
        "from": "new",                    // from state (required)
        "event": "match",                 // event (required)
        "guards": [                       // list/array of guards
          [                               // guard is name of predefined function that returns true or false
            "name": "new-to-matched-guard"
            "arguments": [
              "argument1": ..someValue..
              "argument2": ..someValue..
            ]
          ]
        ],
        "to": "matched",                     // to state (required)
        "actions": [                         // list/array of actions
          [                                  // action is a name of predefined function
            "name": "new-to-matched-action"  // that executes the logic needed by this transition
            "arguments": [
              "argument1": ..someValue..
              "argument2": ..someValue..
            ]
          ]
        ],
        "auto": [                            // .. later
          [                                  // list of conditions, if each returns true then event
              "name": "automatic-condition"  // needs/could to be sent by application without user
              "arguments": [
                "argument1": ..someValue..
                "argument2": ..someValue..
              ]
          ]
        ]
    ],
    ...
  ]
]
```

### Machine definition

<dl>
  <dt>schema</dt>
  <dd>transitions, initialState, finalState, etc.</dd>

  <dt>actions</dt>
  <dd>predefined named actions, <i>todo</i> describe more how action is declared (name, arguments), how its call is defined (explicit/implicit parameters)
  </dd>

  <dt>conditions</dt>
  <dd>???</dd>

  <dt>objectConfiguration</dt>
  <dd>???</dd>
</dl>


### FSM API

```
import com.opuscapita.fsm.Machine
import com.opuscapita.fsm.MachineDefinition

def machineDefinition = new MachineDefinition([schema: schema, actions: actions])
// register workflow
def machine = new Machine([machineDefinition: machineDefinition, context:context]);

// both 'start' and 'sendEvent' return Promise e.g. async execution

// start/initialize workflow
machine.start([object: object])

// list of available events: {event, auto}, e.g. event
// and auto(matic) functions for checking if event should/could be sent automatically
machine.availableTransitions([object: object])

// send event
// returns promise
// in case of successful transition then function will be called with one parameters
// that is an JSON with the following structure:
// - object - object in new state (the same reference that is passed as parameter)
machine.sendEvent([object: object, event: event, request: request])

machine.currentState([object: object])     // gets current state
machine.is([object: object, state: state])         // is object in state
machine.isInFinalState([object: object])   // returns true iff object is in one of final states
machine.can([object: object, event: event])       // whether event is available
machine.cannot([object: object, event: event])    // whether event is not available
```
