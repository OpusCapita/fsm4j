package com.opuscapita.fsm

import com.opuscapita.fsm.artefact.FsmDefinition
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * @author Dmitry Divin
 */
class FsmDefinitionService {
    GrailsApplication grailsApplication
    private Map registry = [:].asSynchronized()

    void register(String workflowName, Class<FsmDefinition> clazz) {
//        registry[workflowName] =
    }

    FsmDefinition getAt(String workflowName) {
        grailsApplication.
    }
}
