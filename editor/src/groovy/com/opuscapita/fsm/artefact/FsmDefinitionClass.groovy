package com.opuscapita.fsm.artefact

import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * Represents <name>FsmDefinition class
 *
 * @author Dmitry Divin
 */
interface FsmDefinitionClass extends GrailsClass {

    /**
     * Get workflow unique identifier
     */
    String getWorkflowName()
}