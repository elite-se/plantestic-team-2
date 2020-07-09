package de.unia.se.plantestic

import com.google.common.io.Resources
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator
import org.eclipse.acceleo.parser.compiler.AcceleoCompiler
import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.nio.file.Paths

class RestAssuredAcceleoGenerator : AbstractAcceleoGenerator() {

    private val moduleName = "/generateCode.mtl"
    private val templateNames = arrayOf("generateTestScenario")
    private val acceleoTransformation = javaClass.classLoader.getResource("code-generation/generateCode.mtl")

    private val acceleoTransformationsInputFolder = Resources.getResource("code-generation").path
    private val compiledAcceleoTransformationsOutputFolder = Paths.get(System.getProperty("user.dir"),
        "build", "classes", "kotlin", "main", "de", "unia", "se", "plantestic").toString()

    val tempDir = createTempDir()

    override fun initialize(model: EObject, targetFolder: File, arguments: List<Any>) {
        println("Compiling .mtl file for generating code from Rest assured Model")
        val acceleoCompiler = AcceleoCompiler()


        acceleoTransformation.openStream().copyTo(File(tempDir, moduleName).outputStream())
        println(tempDir.absoluteFile)
        println(acceleoTransformationsInputFolder)
        println(acceleoTransformationsInputFolder)

        acceleoCompiler.setSourceFolder(tempDir.absolutePath)
        acceleoCompiler.setOutputFolder("/home/max/projects/uni/plantestic/example/src/dist")  // path of emtl directory
        acceleoCompiler.setBinaryResource(false)
        acceleoCompiler.execute()

        if (javaClass.classLoader.getResource("generateCode.mtl.emtl") == null) {
            throw RuntimeException()
        }

        if (javaClass.getResource("/generateCode.mtl.emtl") == null) {
            throw RuntimeException()
        }


        super.initialize(model, targetFolder, arguments)
    }

    override fun registerPackages(resourceSet: ResourceSet?) {
//        val resourceSet = ResourceSetImpl()
//        resourceSet.resourceFactoryRegistry.extensionToFactoryMap["emtl"] = XMIResourceFactoryImpl()
//        val metaModelResource = resourceSet.getResource(URI.createURI(File(tempDir, "generateCode.emtl").absolutePath), true)
//        val metaModelEPackage = metaModelResource.contents[0]
//        resourceSet!!.packageRegistry[EcorePackage.eINSTANCE.nsURI] = metaModelEPackage

        super.registerPackages(resourceSet)
        println(resourceSet!!.packageRegistry.keys)
        println(resourceSet!!.packageRegistry.keys.size)
    }

    @Throws(IOException::class)
    override fun doGenerate(monitor: Monitor) {
//        println(EPackage.Registry.INSTANCE.keys)
        println("Generating code from Rest assured Model")
        super.doGenerate(monitor) // NoGenerationHasOccurred
    }

    override fun getModuleName(): String {
        return moduleName
    }

    override fun getTemplateNames(): Array<String> {
        return templateNames
    }
}
