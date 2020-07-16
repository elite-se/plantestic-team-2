package de.unia.se

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import java.io.File
import java.util.function.Consumer
import javax.inject.Inject

/**
 * Gradle Task that runs the jOOQ source code generation.
 */
open class PlantesticTask @Inject constructor(
    private val projectLayout: ProjectLayout?,
    private val execOperations: ExecOperations?,
    private val plantesticClasspath: FileCollection?,
    private val outputDirectory: String,
    private val inputSourceSet: SourceDirectorySet
) :
    DefaultTask() {

    private var javaExecSpec: Action<in JavaExecSpec?>? = null
    private var execResultHandler: Action<in ExecResult?>? = null

    @Classpath
    fun getPlantesticClasspath(): FileCollection? {
        return plantesticClasspath
    }

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

    @TaskAction
    fun generate() {
        for (file: File in inputSourceSet.matching(PatternSet().include("*.puml"))) {
            val execResult: ExecResult? = executePlantestic(file)
            if (execResultHandler != null) {
                execResultHandler!!.execute(execResult)
            }
        }
    }

    private fun executePlantestic(file: File): ExecResult? {
        return execOperations!!.javaexec { spec ->
            spec.setMain("de.unia.se.plantestic.Main")
            spec.setClasspath(plantesticClasspath)
            spec.setWorkingDir(projectLayout!!.getProjectDirectory())
            spec.args("--input", file.absolutePath, "--output", File(outputDirectory).absolutePath)
            if (javaExecSpec != null) {
                javaExecSpec!!.execute(spec)
            }
        }
    }
}
