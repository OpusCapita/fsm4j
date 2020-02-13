package com.opuscapita.fsm

/**
 * @author Dmitry Divin
 */
class MachineSendEventException extends Exception {
    private Map eventParams

    MachineSendEventException(String message, Map eventParams) {
        super(message)

        this.eventParams = eventParams
    }
}
