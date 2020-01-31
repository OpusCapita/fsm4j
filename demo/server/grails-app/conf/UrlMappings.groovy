class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/api/transitions"(controller: "fsmEditorApi", action: "availableTransitions", method: "POST")
        "/api/editordata"(controller: "fsmEditorApi", action: "editordata", method: "GET")
        "/api/editordata"(controller: "fsmEditorApi", action: "updateSchema", method: "POST")
        "/api/history/${objectId}"(controller: "fsmEditorApi", action: "history", method: "GET")
        "/api/objects"(controller: "fsmEditorApi", action: "objects", method: "GET")
        "/api/event"(controller: "fsmEditorApi", action: "sendEvent", method: "POST")
        "/api/states"(controller: "fsmEditorApi", action: "states", method: "GET")
        "/api/eval"(controller: "fsmEditorApi", action: "eval", method: "POST")
        "/api/logEvents"(controller: "fsmEditorApi", action: "logEvents", method: "GET")

        "/api/health/alive"(controller: "health", action: "alive", method: "GET")
        "/api/health/ready"(controller: "health", action: "ready", method: "GET")

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
