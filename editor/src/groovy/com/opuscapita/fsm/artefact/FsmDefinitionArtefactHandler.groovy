package com.opuscapita.fsm.artefact

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter

/**
 * Detects artefacts that are ended with *FsmDefinition
 *
 * @author Dmitry Divin
 */
class FsmDefinitionArtefactHandler extends ArtefactHandlerAdapter {
    // The name for artifacts in the application
    static final String TYPE = "FsmDefinition"
    // The suffix of all reference search configuration handler classes
    static final String SUFFIX = "FsmDefinition"

    FsmDefinitionArtefactHandler() {
        super(TYPE, FsmDefinitionClass, DefaultFsmDefinitionClass, SUFFIX)
    }
}
