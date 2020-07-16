package de.unia.se.plantestic

import com.google.common.io.Resources
import io.kotlintest.specs.StringSpec
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl

class PumlParserTest : StringSpec({

    "Parsing works for the minimal example" {
        MetaModelSetup.doSetup()

        PumlParser.parse(MINIMAL_HELLO_INPUT_PATH)
    }

    "Parsing works for the complex hello example" {
        MetaModelSetup.doSetup()

        PumlParser.parse(COMPLEX_HELLO_INPUT_PATH)
    }

    "Parsing works for the rerouting example" {
        MetaModelSetup.doSetup()

        PumlParser.parse(REROUTE_INPUT_PATH)
    }

    "Parsing works for the xcall example" {
        MetaModelSetup.doSetup()

        PumlParser.parse(XCALL_INPUT_PATH)
    }
}) {
    companion object {
        private val MINIMAL_HELLO_INPUT_PATH = Resources.getResource("minimal_hello.puml").path
        private val COMPLEX_HELLO_INPUT_PATH = Resources.getResource("complex_hello.puml").path
        private val REROUTE_INPUT_PATH = Resources.getResource("rerouting.puml").path
        private val XCALL_INPUT_PATH = Resources.getResource("xcall.puml").path

        fun printModel(model: EObject) {
            val resource = ResourceSetImpl().createResource(URI.createURI("dummy:/test.ecore"))
            resource.contents.add(model)

            resource.save(System.out, null)
        }
    }
}
