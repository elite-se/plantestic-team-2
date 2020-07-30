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
    "End2End test receives request on mock server for the minimal hello".config(enabled = false) {
        wireMockServer.stubFor(
            get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withStatus(200)))
        wireMockServer.stubFor(get(urlPathEqualTo("/swagger/openapi.yaml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile(MINIMAL_SWAGGER_FILE.toString())))

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

    // This test is the greatest test the world has ever seen. Behind me even grown man that have never cried before in
    // their lives - not even when they were babies - cried when I signed this bill.
    "End2End test project-eden" {
        wireMockServer.stubFor(
                post(urlMatching("/gardener/doplant/([a-z]*)"))
                        .willReturn(aResponse()
                                .withStatus(200)))
        wireMockServer.stubFor(
                get(urlMatching("/garden/plant/exists/([a-z]*)"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"exists\": true }")))
        wireMockServer.stubFor(
                get(urlMatching("/PflanzenKoelle/buy/([a-z]*)"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"plant\": \"pflonzn\"}")))
        wireMockServer.stubFor(
                post(urlMatching("/garden/plant/([a-z]*)"))
                        .willReturn(aResponse()
                                .withStatus(200)))
        wireMockServer.stubFor(
                get(urlMatching("/owner/isplanted/([a-z]*)"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody("{ \"success\": true }")))

        wireMockServer.stubFor(get(urlPathEqualTo("/swagger/project-eden.yaml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile(EDEN_SWAGGER_FILE.toString())))

        runTransformationPipeline(EDEN_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Test_project_eden_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create()
        compiledTest.call("setup")
        compiledTest.call("test1")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 3
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents[1].response.status shouldBe 200
        wireMockServer.allServeEvents[2].response.status shouldBe 200
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Resources.getResource("minimal_hello.puml").path)
        private val MINIMAL_SWAGGER_FILE = File(Resources.getResource("openapi.yaml").path)
        private val EDEN_INPUT_FILE = File(Resources.getResource("project-eden.puml").path)
        private val EDEN_SWAGGER_FILE = File(Resources.getResource("project-eden.yaml").path)
        private val OUTPUT_FOLDER = File(Resources.getResource("code-generation").path + "/End2EndTests/GeneratedCode")
    }

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }
}
