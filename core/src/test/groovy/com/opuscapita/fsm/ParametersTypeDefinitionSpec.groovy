package com.opuscapita.fsm

import spock.lang.Specification
import static ParametersTypeDefinition.*

/**
 * @author Dmitry Divin
 */
class ParametersTypeDefinitionSpec extends Specification {
    def "should validate"() {
        when:
        skipNullParameters().validate(null)

        then:
        noExceptionThrown()

        when:
        skipNullParameters().validate([:])

        then:
        noExceptionThrown()
    }

    def "should parameter required"() {
        when:
        createTypeDefinition().isRequired("a").validate([a: "b"])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().isRequired("a").validate([:])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().isRequired("a").validate(null)

        then:
        thrown(NullPointerException)

        when:
        createTypeDefinition().isRequired("a.b").validate([a: "b"])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().isRequired("a.b").validate([a: [b: "c"]])

        then:
        noExceptionThrown()
    }

    def "should parameter type cast"() {
        when:
        createTypeDefinition().withType("a", Collection).validate([a: []])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().withType("a.b", Collection).validate([a: [b: []]])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().withType("a", Collection).validate([a: [] as Set])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().withType("a.b", Collection).validate([a: [b: [] as Set]])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().withType("a", List).validate([a: [] as Set])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().withType("a.b", List).validate([a: [b: [] as Set]])

        then:
        thrown(IllegalParameterException)
    }

    def "should parameter is not empty"() {
        when:
        createTypeDefinition().isNotEmpty("a").validate([a: 0])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: null])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: ""])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: []])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: [0]])

        then:
        noExceptionThrown()

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: [:]])

        then:
        thrown(IllegalParameterException)

        when:
        createTypeDefinition().isNotEmpty("a").validate([a: [b: []]])

        then:
        noExceptionThrown()
    }
}
