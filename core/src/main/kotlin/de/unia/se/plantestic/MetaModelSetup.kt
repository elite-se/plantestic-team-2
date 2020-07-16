package de.unia.se.plantestic

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import xyz.elite.xtext.languages.plantuml.PlantUMLStandaloneSetup
import java.io.File

object MetaModelSetup {

    private val REQUEST_RESPONSE_PAIRS_METAMODEL_URI =
        URI.createURI(
            javaClass.classLoader.getResource("metamodels/reqrespairs/RequestResponsePairs.ecore").toString(),
            true
        )
    private val REST_ASSURED_METAMODEL_URI =
        URI.createURI(
            javaClass.classLoader.getResource("metamodels/restassured/RestAssured.ecore").toString(),
            true
        )

    fun doSetup() {
        PlantUMLStandaloneSetup.doSetup()

        registerMetamodelFromEcoreFile(REST_ASSURED_METAMODEL_URI)
        registerMetamodelFromEcoreFile(REQUEST_RESPONSE_PAIRS_METAMODEL_URI)
    }

    private fun registerMetamodelFromEcoreFile(uri: URI) {
        val resourceSet = ResourceSetImpl()

        val metaModelResource = resourceSet.getResource(uri, true)
        val metaModelEPackage = metaModelResource.contents[0]
        require(metaModelEPackage is EPackage) { "Metamodel for URI $uri wasn't loaded properly!" }
        EPackage.Registry.INSTANCE[metaModelEPackage.nsURI] = metaModelEPackage
    }
}
