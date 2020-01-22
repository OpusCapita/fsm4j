package com.opuscapita.fsm.artefact

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * Default implementation meta information about artefact that are ended *FsmDefinition
 *
 * @author Dmitry Divin
 */
class DefaultFsmDefinitionClass extends AbstractInjectableGrailsClass implements FsmDefinitionClass {
    DefaultFsmDefinitionClass(Class<?> clazz) {
        super(clazz, FsmDefinitionArtefactHandler.SUFFIX)
    }

    @Override
    String getWorkflowName() {
        Object workflowName = GrailsClassUtils.getStaticPropertyValue(getClazz(), "workflowName");
        if (workflowName == null) {
            String className = getClazz().simpleName
            //for example InvoiceApprovalFsmDefinition -> InvoiceApproval
            return className - FsmDefinitionArtefactHandler.SUFFIX
        } else {
            return workflowName.toString()
        }
    }
}
