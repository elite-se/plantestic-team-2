package de.unia.se.plantestic

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File

object Main {

    class Cli : CliktCommand(
        printHelpOnEmptyArgs = true,
        help = "Plantestic is a tool that transforms PlantUML sequence diagrams of REST API calls into Java unit tests.",
        epilog = """
        |DIAGRAM FORMAT
        |==============
        |
        |Plantestic will recognize requests or responses on PlantUML messages or return statements, iff
        |they appear immediately after the : or, in the case of return, immediately after the return keyword.
        |Additionally, requests and responses must adher to a certain syntax.
        |For requests, this is:
        |
        |   request(<METHOD>, "<PATH>"[, {<PARAM_NAME>: "<PARAM_VALUE>" [, ...]}])
        |
        |Where <METHOD> is one of 'GET', 'POST', 'PUT', 'DELETE', 'PATCH' and <PATH> is the relative URL.
        |Valid requests are, for example:
        |
        | * request(GET, "/event/{id}")
        | * request(POST, "/event/create", { id: 42, label: "Event label" })
        |
        |All values that are in quotes can contain arbitrary variable substitutions of the form "$\{VARIABLE_NAME\}".
        |Variables can either be imported via the configuration file or from previous responses.
        |
        |Similarly, responses need to follow the following schema:
        |
        |	response(<CODE> [<DESCRIPTION>] [|| <CODE> [<DESCRIPTION>]]* [, {<VARIABLE_NAME>: "<XPATH_TO_VARIABLE>" [, ...]}]
        |
        |Where <CODE> is any HTTP response code and <DESCRIPTION> is an optional description of the response code.
        |Arbitrarily many acceptable response <CODE> [<DESCRIPTION>] pairs may be specified, separated with ||. 
        |<XPATH_TO_VARIABLE> is a path to a certain value in a JSON or XML body following the XPATH scheme.
        |The value of <XPATH_TO_VARIABLE> will be checked for being present and will then be assigned to
        |<VARIABLE_NAME> for later use. Valid responses are, for example:
        |
        | * response(200)
        | * response(410 GONE)
        | * response(200, { name: "/name" })
        |
        |
        |
        |Plantestic also permits arbitrarily nested conditional requests. They should be put in
        |PlantUML group blocks. The condition can be any valid JavaScript code with any templating variables.
        |Before the code is evaluated, the templating engine will replace all variables in "$\{VAR\}".
        |The condition should return a boolean result, i.e., either true or false. A valid conditional
        |request specification is, f.e.:
        |
        |Alice -> Bob : request(GET, "name")
        |alt weather==sunny
        |	Bob -> Alice : response(200, { name: "/name" })
        |else
        |	Bob -> Alice : resposne(400 FORBIDDEN)
        |end group	
        |
        |
        |
        |Plantestic supports specifying delays using a modifier PlantUML delay syntax:
        |
        |	... wait(<TIME> <UNIT>)
        |
        |Valid delays for example include:
        |
        | * ... wait(10s)
        | * ... wait(500ms)
        |
        |
        |It is also possible to specify async requests/responses, by prepending them with
        |
        |	async[<ID>, <TIMEOUT>]
        |
        |LEGAL
        |=====
        |
        |This software is licensed under Apache 2.0 license and was originally developed by 
        |Andreas Zimmerer, Stefan Grafberger, Fiona Guerin, Daniela Neupert, Michelle Martin
        |
        |The following people built upon their existing work and proudly present this new version
        |of plantestic with heavy internal modifications, refactorings and extensions:
        |Alexander Zellner, Maximilian Ammann, Elias Keis, Dominik Horn
        """.trimMargin()) {

        private val input: String by option(help = "Path to the PlantUML file containing the API specification.")
            .required()

        private val output: String by option(help = "Output folder where the test cases should be written to. Default is './plantestic-test'")
            .default("./plantestic-test")

        override fun run() {
            val inputFile = File(input).normalize()
            val outputFolder = File(output).normalize()

            if (!inputFile.exists()) {
                echo("Input file ${inputFile.absolutePath} does not exist.")
                return
            }

            runTransformationPipeline(inputFile, outputFolder)
        }
    }

    fun runTransformationPipeline(inputFile: File, outputFolder: File) {
        MetaModelSetup.doSetup()

        val pumlDiagramModel = PumlParser.parse(inputFile.absolutePath)

		val testScenarioModel = M2MTransformer.transformPuml2TestScenario(pumlDiagramModel)

        println("Generating code into $outputFolder")
        AcceleoCodeGenerator.generateCode(testScenarioModel, outputFolder)
    }

    @JvmStatic
    fun main(args: Array<String>) = Cli().main(args)
}
