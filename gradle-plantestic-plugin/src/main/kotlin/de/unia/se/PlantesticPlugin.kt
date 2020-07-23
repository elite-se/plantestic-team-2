package de.unia.se

import com.diffplug.gradle.p2.AsMavenPlugin
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
                        outputDirectoryName, extension.sourceSet
                    )
                plantesticTask.description = "Generates the plantestic testcases"
                plantesticTask.group = "plantestic"
            }
        )

        project.plugins.apply("java")

        addEclipseDependencies(project)
        addOutputToTestSources(project, outputDirectoryName)
        addPlantesticDependencies(plantesticRuntime, project)
        addJUnit(project)
    }

    private fun addEclipseDependencies(project: Project) {
        project.plugins.apply("com.diffplug.p2.asmaven")
        val extension = project.plugins.apply(AsMavenPlugin::class.java).extension()
        extension.group("eclipse-p2") {
            it.repo("http://download.eclipse.org/releases/2020-06")
            it.feature("org.eclipse.emf.ecore")
            it.feature("org.eclipse.m2m.qvt.oml.sdk")
            it.feature("org.eclipse.uml2.sdk")
            it.feature("org.eclipse.acceleo")
        }
    }

    private fun addOutputToTestSources(project: Project, outputDirectoryName: String) {
        val sourceSets = project.properties["sourceSets"] as SourceSetContainer?
        val sourceSet = sourceSets!!.getByName("test")
        sourceSet.java.srcDir(outputDirectoryName)
    }

    private fun addJUnit(project: Project) {
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
        project.dependencies.add("testImplementation", "com.atlassian.oai:swagger-request-validator-core:2.10")
        project.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter-engine:5.3.1")
        project.dependencies.add("testImplementation", "com.atlassian.oai:swagger-request-validator-restassured:2.10.0")
    }
}
