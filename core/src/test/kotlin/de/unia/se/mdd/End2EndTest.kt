package de.unia.se.mdd

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.google.common.io.Resources
import de.unia.se.mdd.Main.runTransformationPipeline
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.joor.Reflect
import java.io.File

val wireMockServer = WireMockServer(8080)

class End2EndTest : StringSpec({

    "End2End test works for the minimal hello".config(enabled = true) {
        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_PATH)
    }

    "End2End test produces valid Java code for the minimal hello".config(enabled = true) {
        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_PATH)

        // Now compile the resulting code
        Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/minimal_hello.java").readText())
            .create(MINIMAL_EXAMPLE_CONFIG_PATH)
    }

    "End2End test receives request on mock server for the minimal hello".config(enabled = true) {
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/B/hello"))
                .willReturn(WireMock.aResponse().withStatus(200)))

        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_PATH)

        // Now compile the resulting code and execute it
        val compiledTest = Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/minimal_hello.java").readText())
            .create(MINIMAL_EXAMPLE_CONFIG_PATH)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
    }

    "End2End test works for complex hello".config(enabled = true) {
        runTransformationPipeline(COMPLEX_HELLO_INPUT_PATH)
    }

    "End2End test produces valid Java code for complex hello".config(enabled = true) {
        runTransformationPipeline(COMPLEX_HELLO_INPUT_PATH)

        // Now compile the resulting code
        Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/complex_hello.java").readText())
            .create(COMPLEX_HELLO_CONFIG_PATH)
    }

    "End2End test receives request on mock server for complex hello".config(enabled = false) {
        val body = """{
            |"itemA" : "value1",
            |"itemB" : "value2",
            |}""".trimMargin()

        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/testReceiver/test/123"))
                .willReturn(WireMock.aResponse().withStatus(200).withBody(body)))

        runTransformationPipeline(COMPLEX_HELLO_INPUT_PATH)

        val generatedCodeText = File("$OUTPUT_PATH/complex_hello.java").readText()
        val compiledTestClass = Reflect.compile("com.mdd.test.Test", generatedCodeText)
        val compiledTestClassObject = compiledTestClass.create(COMPLEX_HELLO_CONFIG_PATH)
        compiledTestClassObject.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
    }

    "End2End works for the rerouting example".config(enabled = true) {
        runTransformationPipeline(REROUTE_INPUT_PATH)
    }

    "End2End test produces valid Java code for the rerouting example".config(enabled = true) {
        runTransformationPipeline(REROUTE_INPUT_PATH)

        // Now compile the resulting code
        Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/rerouting.java").readText())
            .create(REROUTE_CONFIG_PATH)
    }

    "End2End test receives request on mock server for the rerouting example".config(enabled = false) {
        wireMockServer.stubFor(get(urlEqualTo("/hello/123")).willReturn(aResponse().withBody("test")))

        runTransformationPipeline(REROUTE_INPUT_PATH)

        // Now compile the resulting code and execute it
        val compiledTest = Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/scenario.java").readText())
            .create(REROUTE_CONFIG_PATH)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
    }

    "End2End test works for the xcall example".config(enabled = true) {
        runTransformationPipeline(XCALL_INPUT_PATH)
    }

    "End2End test produces valid Java code for the xcall example".config(enabled = true) {
        runTransformationPipeline(XCALL_INPUT_PATH)

        // Now compile the resulting code
        Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/xcall.java").readText())
            .create(XCALL_CONFIG_PATH)
    }

    "End2End test receives request on mock server for the xcall example".config(enabled = false) {
        wireMockServer.stubFor(get(urlEqualTo("/hello/123")).willReturn(aResponse().withBody("test")))

        runTransformationPipeline(XCALL_INPUT_PATH)

        // Now compile the resulting code and execute it
        val compiledTest = Reflect.compile("com.mdd.test.Test", File("$OUTPUT_PATH/scenario.java").readText())
            .create(XCALL_CONFIG_PATH)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_PATH = Resources.getResource("minimal_hello.puml").path
        private val MINIMAL_EXAMPLE_CONFIG_PATH = Resources.getResource("minimal_hello_config.toml").path

        private val COMPLEX_HELLO_INPUT_PATH = Resources.getResource("complex_hello.puml").path
        private val COMPLEX_HELLO_CONFIG_PATH = Resources.getResource("complex_hello_config.toml").path

        private val REROUTE_INPUT_PATH = Resources.getResource("rerouting.puml").path
        private val REROUTE_CONFIG_PATH = Resources.getResource("rerouting_config.toml").path

        private val XCALL_INPUT_PATH = Resources.getResource("xcall.puml").path
        private val XCALL_CONFIG_PATH = Resources.getResource("xcall_config.toml").path

        private val OUTPUT_PATH = Resources.getResource("code-generation").path + "/generatedCode"
    }

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
    }
}
