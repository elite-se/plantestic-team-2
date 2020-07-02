package de.unia.se

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
//import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import java.io.File

/**
 * Gradle Task that runs the jOOQ source code generation.
 */
class JooqTask @javax.inject.Inject constructor(
    projectLayout: ProjectLayout?,
//    execOperations: ExecOperations?,
    jooqClasspath: FileCollection?
) :
    DefaultTask() {
//    private val projectLayout: ProjectLayout?
//    private val execOperations: ExecOperations?
//    private val jooqClasspath: FileCollection?

    private var javaExecSpec: Action<in JavaExecSpec?>? = null
    private var execResultHandler: Action<in ExecResult?>? = null

//    @Classpath
//    fun getJooqClasspath(): FileCollection? {
//        return jooqClasspath
//    }

    @Internal
    fun getJavaExecSpec(): Action<in JavaExecSpec?>? {
        return javaExecSpec
    }

    fun setJavaExecSpec(javaExecSpec: Action<in JavaExecSpec?>?) {
        this.javaExecSpec = javaExecSpec
    }

    @Internal
    fun getExecResultHandler(): Action<in ExecResult?>? {
        return execResultHandler
    }

    fun setExecResultHandler(execResultHandler: Action<in ExecResult?>?) {
        this.execResultHandler = execResultHandler
    }

//    @get:OutputDirectory
//    val outputDirectory: Directory?
//        get() = projectLayout.getProjectDirectory().dir(configuration.getGenerator().getTarget().getDirectory())

    @TaskAction
    fun generate() {
        // define a config file to which the jOOQ code generation configuration is written to
        val configFile = File(getTemporaryDir(), "config.xml")

        // write jOOQ code generation configuration to config file
//        writeConfiguration(normalizedConfiguration, configFile)

        // generate the jOOQ Java sources files using the written config file
//        val execResult: ExecResult? = executeJooq(configFile)

        // invoke custom result handler
        if (execResultHandler != null) {
//            execResultHandler.execute(execResult)
        }
    }

//    private fun writeConfiguration(config: Configuration?, file: File?) {
//        try {
//            FileOutputStream(file).use { fs ->
//                val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
//                val schema: Schema =
//                    sf.newSchema(GenerationTool::class.java.getResource("/xsd/" + Constants.XSD_CODEGEN))
//                val ctx: JAXBContext = JAXBContext.newInstance(Configuration::class.java)
//                val marshaller = ctx.createMarshaller()
//                marshaller!!.schema = schema
//                marshaller.marshal(config, fs)
//            }
//        } catch (e: IOException) {
//            throw TaskExecutionException(this@JooqTask, e)
//        } catch (e: JAXBException) {
//            throw TaskExecutionException(this@JooqTask, e)
//        } catch (e: SAXException) {
//            throw TaskExecutionException(this@JooqTask, e)
//        }
//    }

//    private fun executeJooq(configFile: File?): ExecResult? {
//        return execOperations.javaexec({ spec ->
//            spec.setMain("org.jooq.codegen.GenerationTool")
//            spec.setClasspath(jooqClasspath)
//            spec.setWorkingDir(projectLayout.getProjectDirectory())
//            spec.args(configFile)
//            if (javaExecSpec != null) {
//                javaExecSpec.execute(spec)
//            }
//        })
//    }

//    companion object {
//        private fun relativizeTo(configuration: Configuration?, dir: File?): Configuration? {
//            val directoryValue: String = configuration.getGenerator().getTarget().getDirectory()
//            if (directoryValue != null) {
//                val file = File(directoryValue)
//                if (file.isAbsolute) {
//                    var relativized = dir!!.toURI().relativize(file.toURI()).path
//                    if (relativized.endsWith(File.separator)) {
//                        relativized = relativized.substring(0, relativized!!.length - 1)
//                    }
//                    configuration.getGenerator().getTarget().setDirectory(relativized)
//                }
//            }
//            return configuration
//        }
//    }

//    init {
//        this.projectLayout = projectLayout
//        this.execOperations = execOperations
//        this.jooqClasspath = jooqClasspath
////        this.configuration = configuration
//    }
}
