package de.unia.se

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin

class PlantesticPlugin : Plugin<Project> {
    var project: Project? = null
        private set
    private var plantesticRuntime: Configuration? = null
    private var extension: PlantesticExtension? = null
    private var outputDirectoryName: String? = null
    override fun apply(project: Project) {
        this.project = project
        addPlantesticExtension(project)
        addPlantesticDependencies(project)
        outputDirectoryName = project.buildDir.toString() + "/generated-src/plantestic/"

        createTask()
        configureSourceSet()

        // Add "java" plugin
        project.plugins.apply(JavaBasePlugin::class.java)
    }

    private fun addPlantesticExtension(project: Project) {
        extension = project.extensions.create(
            JOOQ_EXTENSION_NAME,
            PlantesticExtension::class.java
        )
    }

    /**
     * Adds the configuration that holds the classpath to use for invoking plantestic.
     */
    private fun addPlantesticDependencies(project: Project) {
        val plantesticRuntime = project.configurations.create("plantesticRuntime")
        plantesticRuntime.description = "The classpath used to invoke plantestic"
        val runtimeName = plantesticRuntime.getName()



        project.dependencies.add(runtimeName, "org.jooq:jooq-codegen")
        project.dependencies.add(runtimeName, "javax.xml.bind:jaxb-api:2.3.1")
        project.dependencies.add(runtimeName, "javax.activation:activation:1.1.1")
        project.dependencies.add(runtimeName, "com.sun.xml.bind:jaxb-core:2.3.0.1")
        project.dependencies.add(runtimeName, "com.sun.xml.bind:jaxb-impl:2.3.0.1")

        this.plantesticRuntime = plantesticRuntime
    }

    /**
     * Adds the task that runs the code generator in a separate process.
     */
    private fun createTask() {
        val jooqTask = project!!.tasks
            .create(PLANTESTIC_TASK_NAME, PlantesticTask::class.java, plantesticRuntime)
        jooqTask.description = "Generates the plantestic testcases"
        jooqTask.group = "plantestic"
    }

    /**
     * Ensures the Java compiler will pick up the generated sources.
     */
    private fun configureSourceSet() {
        val sourceSet = extension!!.sourceSet
        sourceSet!!.java.srcDirs(listOf(outputDirectoryName))

        //        if (extension.getGenerateSchemaSourceOnCompilation()) {
        project!!.tasks.getByName(sourceSet!!.compileJavaTaskName)
            .dependsOn(PLANTESTIC_TASK_NAME)
        //        }
    }

    companion object {
        const val PLANTESTIC_TASK_NAME = "generate"
        private const val JOOQ_EXTENSION_NAME = "plantestic"
    }
}
