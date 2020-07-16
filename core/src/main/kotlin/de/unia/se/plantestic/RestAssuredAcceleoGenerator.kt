package de.unia.se.plantestic

import com.google.common.io.Resources
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator
import org.eclipse.acceleo.parser.compiler.AcceleoCompiler
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.ecore.EObject
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Paths

class RestAssuredAcceleoGenerator : AbstractAcceleoGenerator() {

    private val moduleName = "/generateCode.mtl"
    private val templateNames = arrayOf("generateTestScenario")
    private val acceleoTransformation = javaClass.classLoader.getResource("code-generation/generateCode.mtl")

    private var tempDir: File? = null

    override fun initialize(model: EObject, targetFolder: File, arguments: List<Any>) {
        println("Compiling .mtl file for generating code from Rest assured Model")
        val acceleoCompiler = AcceleoCompiler()

        tempDir = createTempDir()

        acceleoTransformation.openStream().copyTo(File(tempDir, moduleName).outputStream())

        acceleoCompiler.setSourceFolder(tempDir!!.absolutePath)
        acceleoCompiler.setOutputFolder(tempDir!!.absolutePath)  // path where the .emtl file is put
        acceleoCompiler.setBinaryResource(false)
        acceleoCompiler.execute()

        super.initialize(model, targetFolder, arguments)
    }

    @Throws(IOException::class)
    override fun doGenerate(monitor: Monitor) {
        println("Generating code from Rest assured Model")
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
