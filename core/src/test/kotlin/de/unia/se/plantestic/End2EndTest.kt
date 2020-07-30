package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.common.io.Resources
import de.unia.se.plantestic.Main.runTransformationPipeline
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.joor.Reflect
import java.io.File

val wireMockServer = WireMockServer(8080)

class End2EndTest : StringSpec({
    "End2End test receives request on mock server for the minimal hello" {
        wireMockServer.stubFor(
            get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(200)))
        wireMockServer.stubFor(get(urlPathEqualTo("/swagger/openapi.yaml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile(SWAGGER_FILE.toString())))

        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Test_minimal_hello_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create()
        compiledTest.call("setup")
        compiledTest.call("test1")

        // Only check for hello response
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        val event = wireMockServer.allServeEvents.find { serveEvent -> serveEvent.request.url == "/hello" }
        if (event != null) {
            event.response.status shouldBe 200
        }
    }

    // This test is bullshit because the mock server setup has nothing to do with the actual scenario
    "End2End test receives request on mock server for the project-eden example".config(enabled = false) {
        wireMockServer.stubFor(get(urlEqualTo("/hello/123")).willReturn(aResponse().withBody("test")))

        runTransformationPipeline(XCALL_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Test_xcall_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create()
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Resources.getResource("minimal_hello.puml").path)
        private val XCALL_INPUT_FILE = File(Resources.getResource("xcall.puml").path)
        private val OUTPUT_FOLDER = File(Resources.getResource("code-generation").path + "/End2EndTests/GeneratedCode")
        private val SWAGGER_FILE = File(Resources.getResource("openapi.yaml").path)
    }

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }
}
