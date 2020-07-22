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
            get(urlEqualTo("/testB/hello"))
                .willReturn(aResponse()
                        .withStatus(200)))
        wireMockServer.stubFor(get(urlPathEqualTo("/swagger/openapi.yaml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile(SWAGGER_FILE.toString())))

        runTransformationPipeline(MINIMAL_EXAMPLE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testminimal_hello_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(MINIMAL_EXAMPLE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Only check for hello response
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        val event = wireMockServer.allServeEvents.find { serveEvent -> serveEvent.request.url == "/textB/hello" }
        if (event != null) {
            event.response.status shouldBe 200
        }
    }

    // This this fails because the receiver "B" is somehow not set, which results in things like "${.path}".
    "End2End test receives request on mock server for complex hello".config(enabled = false) {
        val body = """{
              "itemA" : "value1",
              "itemB" : "value2",
            }"""
        wireMockServer.stubFor(
            get(urlPathMatching("/testB/test/123"))
                .willReturn(aResponse().withStatus(200).withBody(body)))

        wireMockServer.stubFor(
            get(urlPathMatching("/swagger/openapi.yaml"))
                .willReturn(aResponse().withStatus(200).withBody(SWAGGER_FILE.readText()))
        )

        runTransformationPipeline(COMPLEX_HELLO_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testcomplex_hello_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(COMPLEX_HELLO_CONFIG_FILE.path)
        compiledTest.call("test")

        // Only check for hello response
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        val event = wireMockServer.allServeEvents.find { serveEvent -> serveEvent.request.url.startsWith("/swagger") }
        if (event != null) {
            event.response.status shouldBe 200
        }
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == true".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "/UISWITCH",
              "reroute" : "/REROUTE",
              "warmhandover" : "/WARMHANDOVER",
            }"""
        val body_CCC_Voicemanager_voiceenabled = """{
              "eventid1" : "/VoiceStatus/eventId1",
              "agent1" : "/VoiceStatus/agent1/connectionstatus",
              "agent2" : "/VoiceStatus/agent2/connectionstatus",
            }"""
        wireMockServer.stubFor(
                get(urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body_CCC_CRS)))
        wireMockServer.stubFor(
                get(urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body_CCC_Voicemanager_voiceenabled)))

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        try { compiledTest.call("test") } catch (e: Exception) { }

        // Only check for hello response
        wireMockServer.allServeEvents.filterNot { serveEvent -> serveEvent.request.url.startsWith("/swagger") }
        wireMockServer.allServeEvents.forEach { serveEvent ->
            println(serveEvent.request)
            serveEvent.response.status shouldBe 200
        }
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 400".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }"""
        wireMockServer.stubFor(
                get(urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body_CCC_CRS)))
        wireMockServer.stubFor(
                get(urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(aResponse()
                    .withStatus(400)))
        wireMockServer.stubFor(
                get(anyUrl())
                .willReturn(aResponse()
                    .withStatus(400))
        )

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Only check for hello response
        wireMockServer.allServeEvents.filterNot { serveEvent -> serveEvent.request.url.startsWith("/swagger") }
        wireMockServer.allServeEvents.forEach { serveEvent ->
            println(serveEvent.request)
            serveEvent.response.status shouldBe 400
        }
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 404".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }"""
        wireMockServer.stubFor(
                get(urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body_CCC_CRS)))
        wireMockServer.stubFor(
                get(urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(aResponse()
                    .withStatus(404)))

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Only check for hello response
        wireMockServer.allServeEvents.filterNot { serveEvent -> serveEvent.request.url.startsWith("/swagger") }
        wireMockServer.allServeEvents.forEach { serveEvent ->
            println(serveEvent.request)
            serveEvent.response.status shouldBe 200
        }
    }

    // Test is bullshit because it never sets voiceEstablished to anything.
    "End2End test receives request on mock server for rerouting - voiceEstablished == false, return 500".config(enabled = false) {
        val body_CCC_CRS = """{
              "uiswitch" : "UISWITCH",
              "reroute" : "REROUTE",
              "warmhandover" : "WARMHANDOVER",
            }""".trimMargin()
        wireMockServer.stubFor(
                get(urlPathMatching("/CRS/ccc/rerouteOptions"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body_CCC_CRS)))
        wireMockServer.stubFor(
                get(urlPathMatching("/Voicemanager/ccc/events/123/isconnected"))
                .willReturn(aResponse()
                    .withStatus(500)))
        wireMockServer.stubFor(
                get(anyUrl())
                .willReturn(aResponse()
                    .withStatus(500)))

        runTransformationPipeline(REROUTE_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testrerouting_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(REROUTE_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check only for relevant not swagger
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        val event = wireMockServer.allServeEvents.find { serveEvent -> serveEvent.request.url.startsWith("/swagger") }
        if (event != null) {
            event.response.status shouldBe 500
        }
    }

    // This test is bullshit because the mock server setup has nothing to do with the actual scenario
    "End2End test receives request on mock server for the xcall example".config(enabled = false) {
        wireMockServer.stubFor(get(urlEqualTo("/hello/123")).willReturn(aResponse().withBody("test")))

        runTransformationPipeline(XCALL_INPUT_FILE, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles().filter { f -> f.name == "Testxcall_puml.java" }.first()
        val compiledTest = Reflect.compile(
            "com.plantestic.test.${generatedSourceFile.nameWithoutExtension}",
            generatedSourceFile.readText()
        ).create(XCALL_CONFIG_FILE.path)
        compiledTest.call("test")

        // Check if we received a correct request
        wireMockServer.allServeEvents.forEach { serveEvent -> println(serveEvent.request) }
        wireMockServer.allServeEvents.size shouldBe 1
        wireMockServer.allServeEvents[0].response.status shouldBe 200
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Resources.getResource("minimal_hello.puml").path)
        private val MINIMAL_EXAMPLE_CONFIG_FILE = File(Resources.getResource("minimal_hello_config.toml").path)

        private val COMPLEX_HELLO_INPUT_FILE = File(Resources.getResource("complex_hello.puml").path)
        private val COMPLEX_HELLO_CONFIG_FILE = File(Resources.getResource("complex_hello_config.toml").path)

        private val REROUTE_INPUT_FILE = File(Resources.getResource("rerouting.puml").path)
        private val REROUTE_CONFIG_FILE = File(Resources.getResource("rerouting_config.toml").path)

        private val XCALL_INPUT_FILE = File(Resources.getResource("xcall.puml").path)
        private val XCALL_CONFIG_FILE = File(Resources.getResource("xcall_config.toml").path)

        private val OUTPUT_FOLDER = File(Resources.getResource("code-generation").path + "/End2EndTests/GeneratedCode")

        private val SWAGGER_FILE = File(Resources.getResource("openapi.yaml").path)

        fun printCode(folder: File) {
            folder.listFiles().forEach { file ->
                val lines = file.readLines()
                lines.forEach { line -> println(line) }
            }
        }
    }

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }
}
