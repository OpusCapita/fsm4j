package com.opuscapita.fsm.artefact

/**
 * FSM definition interface
 *
 * @author Dmitry Divin
 */
interface FsmDefinition {
    Map getSchema()

    void setSchema(Map updatedSchema)

    Map getObjectConfiguration()

    Map getActions()

    Map getConditions()
}