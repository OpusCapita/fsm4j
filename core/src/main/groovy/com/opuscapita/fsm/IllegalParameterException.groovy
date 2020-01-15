package com.opuscapita.fsm

/**
 * The exception throw when parameter is not valid
 *
 * @author Dmitry Divin
 */
class IllegalParameterException extends Exception {
    String parameterName
    def parameterValue

    IllegalParameterException(String message, String parameterName, parameterValue) {
        super(message)
        this.parameterName = parameterName
        this.parameterValue = parameterValue
    }
}
