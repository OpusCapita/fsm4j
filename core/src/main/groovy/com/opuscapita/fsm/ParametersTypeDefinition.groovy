package com.opuscapita.fsm

/**
 * The class representation parameters type definition
 *
 * @author Dmitry Divin
 */
class ParametersTypeDefinition {
    private final List<Closure> rules = [

    ]

    ParametersTypeDefinition(List rules) {
        this.rules.addAll(rules)
    }

    static ParametersTypeDefinition createTypeDefinition() {
        new ParametersTypeDefinition([
                { Map params ->
                    if (params == null) {
                        throw new NullPointerException("Parameters can't be null")
                    }
                    return params
                }
        ])
    }

    static ParametersTypeDefinition skipNullParameters() {
        //return empty map as default
        new ParametersTypeDefinition([
                { Map params ->
                    if (params == null) {
                        return [:]
                    }
                    return params
                }])
    }

    static getValue(Map params, String parameterName) {
        List<String> parameterNamePath = parameterName.split("\\.")
        if (parameterNamePath.size() == 1) {
            return params[parameterName]
        } else {
            String parentParameterName = parameterNamePath.subList(0, parameterNamePath.size() - 1).join(".")
            def parent = getValue(params, parentParameterName)
            if (parent == null) {
                return null
            } else if (parent instanceof Map) {
                return parent[parameterNamePath.last()]
            } else {
                throw new IllegalParameterException("Illegal parameter [${parentParameterName}] Type: expected [${Map.name}], but was [${parent.class.name}]", parentParameterName, parent)
            }
        }
    }

    /**
     * Add Type cast parameter definition
     *
     * @param parameterName - parameter name
     * @param type - required type
     * @return current parameters type definition
     */
    ParametersTypeDefinition withType(String parameterName, Class type) {
        rules << { Map params ->
            def parameterValue = getValue(params, parameterName)
            if (parameterValue != null && !type.isInstance(parameterValue)) {
                throw new IllegalParameterException("Illegal parameter [${parameterName}] Type: expected [${type.name}], but was [${parameterValue.getClass().name}]", parameterName, parameterValue)
            }
            return params
        }
        return this
    }

    /**
     * Add required parameter definition
     *
     * @param parameterName - parameter name
     * @return current parameters type definition
     */
    ParametersTypeDefinition isRequired(String parameterName) {
        rules << { Map params ->
            def parameterValue = getValue(params, parameterName)
            if (parameterValue == null) {
                throw new IllegalParameterException("Parameter [${parameterName}] is mandatory", parameterName, parameterValue)
            }
            return params
        }

        return this
    }

    /**
     * Is not empty parameter definition
     *
     * @param parameterName - parameter name
     * @return current parameters type definition
     */
    ParametersTypeDefinition isNotEmpty(String parameterName) {
        rules << { Map params ->
            def parameterValue = getValue(params, parameterName)
            if (parameterValue != null && (
                    "${parameterValue}".isEmpty() ||
                            (parameterValue instanceof Collection && parameterValue.isEmpty()) ||
                            (parameterValue instanceof Map && parameterValue.isEmpty())
            )) {
                throw new IllegalParameterException("Parameter [${parameterName}] is not empty", parameterName, parameterValue)
            }
            return params
        }

        return this
    }


    /**
     * Validate parameters according to declared parameter definitions
     *
     * @param params - validated parameters, is optional
     * @throws IllegalParameterException - throw error if any parameter is not valid
     */
    void validate(Map params) throws IllegalParameterException {
        rules.inject(params, { Map acc, Closure closure -> closure(acc) })
    }
}
