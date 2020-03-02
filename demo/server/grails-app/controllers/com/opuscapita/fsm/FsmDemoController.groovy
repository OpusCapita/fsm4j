package com.opuscapita.fsm

/**
 * The controller render demo editor
 *
 * @author Dmitry Divin
 */
class FsmDemoController {
    def index() {
        String publicUrlVariable = System.getenv("BASE_URL")
        String publicUrl = g.createLinkTo(controller: "fsmDemo")
        String baseUrl = publicUrlVariable?:publicUrl

        render view: "index", model: [baseUrl: baseUrl]
    }
}
