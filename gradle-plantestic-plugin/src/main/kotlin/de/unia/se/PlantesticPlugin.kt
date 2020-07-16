package de.unia.se

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet

@Suppress("unused")
class PlantesticPlugin : Plugin<Project> {

    companion object {
        const val PLANTESTIC_TASK_NAME = "generate"
        private const val PLANTESTIC_EXTENSION_NAME = "plantestic"
    }

    override fun apply(project: Project) {
        val plantesticRuntime = project.configurations.create("plantesticRuntime")
        plantesticRuntime.description = "The classpath used to invoke plantestic"

        project.extensions.create(
            PLANTESTIC_EXTENSION_NAME,
            PlantesticExtension::class.java,
            { extension: PlantesticExtension ->
                val outputDirectoryName: String = "${project.buildDir}/generated-src/plantestic/"

                val plantesticTask = project.tasks
                    .create(PLANTESTIC_TASK_NAME, PlantesticTask::class.java, plantesticRuntime,
                        outputDirectoryName, extension.sourceSet!!.resources)
                plantesticTask.description = "Generates the plantestic testcases"
                plantesticTask.group = "plantestic"


                val sourceSet: SourceSet = extension.sourceSet!!
                sourceSet.java.srcDir(plantesticTask)
            }
        )


        addPlantesticDependencies(plantesticRuntime, project)

        // Add "java" plugin
        project.plugins.apply(JavaBasePlugin::class.java)
    }

    /**
     * Adds the configuration that holds the classpath to use for invoking plantestic.
     */
    private fun addPlantesticDependencies(plantesticRuntime: Configuration, project: Project) {
//        project.dependencies.project(mapOf("path" to ":core"))

        project.repositories.maven {
            it.setUrl("https://maven.wso2.org/nexus/content/groups/wso2-public/")
        }

        project.repositories.maven {
            it.setUrl("https://repo.eclipse.org/content/groups/releases/")
        }

        project.dependencies.add(plantesticRuntime.name, "de.unia.se:core:0.1")
    }
}
