package de.unia.se.plantestic

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import xyz.elite.xtext.languages.plantuml.plantUML.Model

object PumlParser {

    /**
     * Parses a resource specified by an URI and returns the resulting object tree root element.
     * @param inputUri URI of resource to be parsed as String
     * @return Root model object
     */
    fun parse(inputUri: String): Model {
        require(EPackage.Registry.INSTANCE["http://www.elite.xyz/xtext/languages/plantuml/PlantUML"] != null) {
            "Please run MetaModelSetup.doSetup() first!"
        }

        val uri = URI.createFileURI(inputUri)
        val resource = ResourceSetImpl().getResource(uri, true)

        // Resolve cross references
        EcoreUtil.resolveAll(resource)

				// TODO: print errrors from parsing puml (see xtext language testcases)

        require(resource.contents.size > 0) { "File should contain something meaningful." }
        require(resource.contents[0] is Model) { "File should contain a diagram." }
        return resource.contents[0] as Model
    }
}
