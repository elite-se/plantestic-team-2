package de.unia.se.plantestic;

import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.ecore.EObject
import java.io.File
import java.io.IOException
import java.net.URL

class RestAssuredAcceleoGenerator : AbstractAcceleoGenerator() {

    private val moduleName = "/generateCode.mtl"
    private val templateNames = arrayOf("generateTestScenario")
    private val acceleoTransformation = javaClass.classLoader.getResource("code-generation/generateCode.mtl")

    private var tempDir: File? = null

    override fun initialize(model: EObject, targetFolder: File, arguments: List<Any>) {
        println("Compiling .mtl file for generating code")
        val acceleoCompiler = AcceleoCompiler()

        tempDir = createTempDir()

        acceleoTransformation.openStream().copyTo(File(tempDir, moduleName).outputStream())

        acceleoCompiler.setSourceFolder(tempDir!!.absolutePath)
        acceleoCompiler.setOutputFolder(tempDir!!.absolutePath)  // path where the .emtl file is put
        acceleoCompiler.binaryResource = false
        acceleoCompiler.execute()

        super.initialize(model, targetFolder, arguments)
    }

    @Throws(IOException::class)
    override fun doGenerate(monitor: Monitor) {
        println("Generating code")
        super.doGenerate(monitor)
    }

    override fun findModuleURL(moduleName: String?): URL? {
        return File(tempDir, "generateCode.emtl").toURI().toURL()
    }

    override fun getModuleName(): String {
        return moduleName
    }

    override fun getTemplateNames(): Array<String> {
        return templateNames
    }
}
