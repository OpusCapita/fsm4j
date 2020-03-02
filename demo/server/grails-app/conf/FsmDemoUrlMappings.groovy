class FsmDemoUrlMappings {
    static mappings = {
        "/fsmDemo"(controller: "fsmDemo", action: "index")
        "/fsmDemo/**"(controller: "fsmDemo")
        "/"(redirect: [controller: "fsmDemo", action: "index"])
    }
}