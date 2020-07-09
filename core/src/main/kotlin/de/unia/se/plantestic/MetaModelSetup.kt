package de.unia.se.plantestic

import com.google.common.io.Resources
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.xtext.resource.ClasspathUriResolutionException
import plantuml.PumlStandaloneSetup
import java.io.File

object MetaModelSetup {

//    private val REQUEST_RESPONSE_PAIRS_METAMODEL_URI =
//        URI.createURI(Resources.getResource("metamodels/reqrespairs/RequestResponsePairs.ecore").toExternalForm())
//    private val REST_ASSURED_METAMODEL_URI =
//        URI.createURI(Resources.getResource("metamodels/restassured/RestAssured.ecore").toExternalForm())
//    private val REQUEST_RESPONSE_PAIRS_METAMODEL_URI =
//        URI.createFileURI("/home/max/projects/uni/plantestic/core/build/resources/main/metamodels/reqrespairs/RequestResponsePairs.ecore")
//
//    private val REST_ASSURED_METAMODEL_URI =
//        URI.createFileURI("/home/max/projects/uni/plantestic/core/build/resources/main/metamodels/restassured/RestAssured.ecore")

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
        println(REST_ASSURED_METAMODEL_URI)
        println(REQUEST_RESPONSE_PAIRS_METAMODEL_URI)
        PumlStandaloneSetup.doSetup()

        val tempDir = createTempDir()
        val RestAssured = File(tempDir, "RestAssured.ecore")
        javaClass.classLoader.getResource("metamodels/restassured/RestAssured.ecore").openStream().copyTo(RestAssured.outputStream())
        val RequestResponsePairs = File(tempDir, "RequestResponsePairs.ecore")
        javaClass.classLoader.getResource("metamodels/reqrespairs/RequestResponsePairs.ecore").openStream().copyTo(RequestResponsePairs.outputStream())

        registerMetamodelFromEcoreFile(URI.createFileURI(RestAssured.absolutePath))
        registerMetamodelFromEcoreFile(URI.createFileURI(RequestResponsePairs.absolutePath))
//        registerMetamodelFromEcoreFile(REQUEST_RESPONSE_PAIRS_METAMODEL_URI)
//        registerMetamodelFromEcoreFile(REST_ASSURED_METAMODEL_URI)
    }

    private fun registerMetamodelFromEcoreFile(uri: URI) {
        val resourceSet = ResourceSetImpl()

        val metaModelResource = resourceSet.getResource(uri, true)
        val metaModelEPackage = metaModelResource.contents[0]
        require(metaModelEPackage is EPackage) { "Metamodel for URI $uri wasn't loaded properly!" }
        EPackage.Registry.INSTANCE[metaModelEPackage.nsURI] = metaModelEPackage
    }
}
