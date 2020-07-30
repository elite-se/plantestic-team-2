package de.unia.se.plantestic

import java.io.File
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.resource.Resource
import xyz.elite.xtext.languages.plantuml.plantUML.Model

object PumlParser {

    /**
     * Parses a resource specified by an URI and returns the resulting object tree root element.
     * @param inputUri URI of resource to be parsed as String
     * @return Root model object
     */
    fun parse(inputUri: String): Model {
    	println("Parsing PUML file \"" + inputUri + "\"")
		
        require(EPackage.Registry.INSTANCE["http://elite-se.xyz/plantestic/PlantUML"] != null) {
            "Please run MetaModelSetup.doSetup() first"
        }

        val uri = URI.createFileURI(inputUri)
        val resource = ResourceSetImpl().getResource(uri, true)

        // Resolve cross references
        EcoreUtil.resolveAll(resource)

		// Ensure resource loaded without errors☝︎
		if (resource.getErrors().size > 0) {
			println(displayErrors(resource, inputUri))
			System.exit(-1)
		}
        require(resource.contents.size > 0) { "input PUML file either is empty or could not be properly parsed" }
        return resource.contents[0] as Model
    }
	
	fun displayErrors(resource: Resource, path: String): String  {
		val file = File(path)
		val lines = file.useLines { it.toList() }
		var str = "\n\n"
		
		resource.getErrors().forEach {
			str += "Error in line ${it.getLine()}, column ${it.getColumn()} while parsing ${file.getName()}:\n\n"
			val lineNr = it.getLine()-1;
			str += "  ${lines[lineNr]}\n"
			(0..it.getColumn()).forEach { str += " " }
			str += "\u001B[31m⎺ ${it.getMessage()}\u001B[0m\n"
		}
		
		return str
	}
}
