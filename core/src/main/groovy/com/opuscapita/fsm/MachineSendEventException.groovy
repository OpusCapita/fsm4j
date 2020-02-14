package com.opuscapita.fsm

/**
 * Thrown the error when transition doesn't exist or object can't be released
 *
 * @author Dmitry Divin
 */
class MachineSendEventException extends Exception {
    private Map eventParams

    MachineSendEventException(String message, Map eventParams) {
        super(message)

        this.eventParams = eventParams
    }

    Map getEventParams() {
        return eventParams
    }
}
