package de.unia.se

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.provider.ValueSupplier.ValueProducer.task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import java.io.File


@Suppress("unused")
class PlantesticPlugin : Plugin<Project> {

    companion object {
        const val PLANTESTIC_TASK_NAME = "generate"
        private const val PLANTESTIC_EXTENSION_NAME = "plantestic"
    }

    override fun apply(project: Project) {
        val plantesticRuntime = project.configurations.create("plantesticRuntime")
        plantesticRuntime.description = "The classpath used to invoke plantestic"

        val outputDirectoryName = "${project.buildDir}/generated-src/plantestic/"

        project.extensions.create(
            PLANTESTIC_EXTENSION_NAME,
            PlantesticExtension::class.java,
            { extension: PlantesticExtension ->


                val plantesticTask = project.tasks
                    .create(
                        PLANTESTIC_TASK_NAME, PlantesticTask::class.java, plantesticRuntime,
                        outputDirectoryName, extension.sourceSet!!.resources
                    )
                plantesticTask.description = "Generates the plantestic testcases"
                plantesticTask.group = "plantestic"
            }
        )



        project.plugins.apply("java")

        val sourceSets = project.properties["sourceSets"] as SourceSetContainer?
        val sourceSet = sourceSets!!.getByName("test")
        sourceSet.java.srcDir(outputDirectoryName)

        addPlantesticDependencies(plantesticRuntime, project)

        project.tasks.named("test", Test::class.java) {
            it.useJUnitPlatform()
            it.dependsOn("generate")
        }
    }

    /**
     * Adds the configuration that holds the classpath to use for invoking plantestic.
     */
    private fun addPlantesticDependencies(plantesticRuntime: Configuration, project: Project) {
        project.repositories.mavenCentral()
        project.repositories.mavenLocal()

        project.repositories.maven {
            it.setUrl("https://repo.eclipse.org/content/groups/releases/")
        }

        project.dependencies.add(plantesticRuntime.name, "de.unia.se:core:0.1")
        project.dependencies.add("testImplementation", "io.rest-assured:rest-assured:3.0.0")
        project.dependencies.add("testImplementation", "org.apache.commons:commons-text:1.7")
        project.dependencies.add("testImplementation", "io.rest-assured:rest-assured:3.0.0")
        project.dependencies.add("testImplementation", "javax.xml.bind:jaxb-api:2.3.1")
        project.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter-engine:5.3.1")
    }
}
