# Plantestic

## Description
The test case generator Plantestic produces test cases from a sequence diagram. 
A sequence diagram models a sequence of interactions between objects. 
A test case then checks for such an interaction whether it is implemented as the sequence diagram defines it. 
In an example sequence diagram called `Hello`, let there be two actors Alice and Bob. 
Alice sends Bob the request `GET /hello ` and Bob answers with `Hello World`. 
The corresponding test case now sends an HTTP request `GET /hello` to the backend. 
The test case then expects a response with status `200 OK` and date `Hello World`.

![./core/src/test/resources/minimal_hello.png](./core/src/test/resources/minimal_hello.png)

```
public void test() throws Exception {
		try {
			Response roundtrip1 = RestAssured.given()
					.auth().basic(substitutor.replace("${B.username}"), substitutor.replace("${B.password}"))
				.when()
					.get(substitutor.replace("${B.path}") + substitutor.replace("/hello"))
				.then()
					.assertThat()
					    .statusCode(IsIn.isIn(Arrays.asList(200)));
		} catch (Exception exception) {
			System.out.println("An error occured during evaluating the communication with testReceiver: ");
			exception.printStackTrace();
			throw exception;
		}
	}
```

## Motivation
The implementation of user requirements often deviates from the specification of the same user requirements. 
Individual work, teamwork, and collaboration between teams can produce such a divergence. 
For example, requirements may be misinterpreted or overlooked. 
Teamwork, especially with multiple teams, causes interface errors. 
For example, subsystems of the same product may use conflicting technologies or conflicting data formats.

Our test case generator detects deviations at an early stage: 
The test case generator derives test cases directly from the specification. 
If the implementation fulfills these test cases, then the implementation fulfills the specification. 
If the implementation does not fulfill these test cases, the implementation deviates from the specification. 
With our test case generator, developers can quickly uncover inconsistencies, fix them, and save costs.

## Features
**Plantestic is universal in that it can run in any IDE.**

For this, Plantestic uses Gradle. Original Eclipse dependencies are used! 

**Plantestic is user-friendly.**

You set it up by installing Java and downloading Plantestic.
You generate a test case by filing a sequence diagram and entering one instruction. 

**Plantestic has a powerful condition evaluation.**

A sequence diagram can contain alternative or optional interactions that it invokes under a certain condition. 
Plantestic will evaluate any condition that conforms to JavaScript. 
For this, it uses a JavaScript engine.  

**You can pass parameters to your sequence diagram if you wish to customize its flows.**

For example, you no longer need to reveal security-critical information such as passwords in your sequence diagram. 
Plantestic evaluates the parameters using templating.

## Installation
1. Install Java SE Development Kit 8. 
You can find Java SE Development Kit 8 under the website [https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. Clone the Plantestic repository.
3. Run `./gradlew publish`. This can take a while because the Eclipse repository is cloned.

## How to use

## Usage as Gradle plugin

A Gradle plugin is available in `./gradle-plantestic-plugin`. This plugin is not published to a remote repository currently.
Therefore you need to install it and its dependencies to your local Maven repository. You can to this by executing:

```console
$ ./gradlew publish
```

After that you can copy the `./example` standalone Gradle project. Firstly include the plugin via:

```groovy
plugins {
    id 'de.unia.se.gradle-plantestic-plugin' version "0.1"
}
```

Now you can configure it and enable it:

```groovy
plantestic {
    sourceSet sourceSets.test.resources // Location of the puml files
}
```

You can specify where you put your `.puml` files via the `sourceSet` option.
To run the tests you simply do `./gradlew test` in the root of your project. The first execution can take some time because the P2 Eclipse repository is cloned by the plugin. Consecutive executions take significantly less time.

### Test Specification

#### Sequence Diagram
The input is a PlantUML sequence diagram. 
This sequence diagram contains several participants and interactions between the participants. 
One participiant is the client who calls the test cases. The other participants are services of the implementation. 
In the example diagram, the client is `CCC` and the services are `CRS` and `Voicemanager`.

An interaction contains a request from the client and a response from a service. 
A request contains an HTTP method, a URL, and possibly parameters. 
A response contains an HTTP status code and, if applicable, data. A hyphen separates the HTTP status codes and the data. 

The HTTP method is `GET`, `POST`, or `PUT`. 

The URL path is a String. In it, slashes separate the path components. 
A parameter name in curly braces, preceded by a dollar symbol, thereby specifies parameterized path components. 
Example: ```/path/${param}```

We specify the request parameters in a tuple: 
An opening bracket is followed by the first request parameter. 
This request parameter is followed - comma-separated - by the second request parameter, and so on. 
The last request parameter is followed by a closing bracket.
We specify the request parameter as a `Name: Value` pair: 
The name of the request parameter is followed by a space, a colon, a space, and the value of the request parameter as a string. 
We define the value of the request parameter in curly brackets, preceded by a dollar symbol. 
Example: ```(name1 : "${value1}", name2 : "${value2}")```

We specify the response data in a tuple: 
An opening bracket is followed by the first response datum. 
This response datum is followed - comma-separated - by the second response datum, and so on. 
The last response datum is followed by a closing bracket.
We specify the response datum as a `Name: XPath` pair: 
The name of the response datum is followed by a space, a colon, a space, and the xpath of the response datum as a string. 
In the xpath, slashes separate the path components. . 
Example: ```(name1 : "/value/value1", name2 : "/value2")```

![./core/src/test/resources/rerouting.png](./core/src/test/resources/rerouting.png)

#### Configs

Configs can be prepended to PlantUML files:

```puml
@startconfig
vin = "987"
eventid = "123"
@endconfig

@startconfig
vin = "120"
eventid = "20"
@endconfig

SEQUENCE @startuml

participant CCCMock
...
```

For each config a separate test case is generated.

#### Environment variables

All environment variables are accessible in the sequence diagrams via string substitution.
This means you can add the text `${SECRET_PASSWORD}` in the sequence diagram and export the variable before running the tests via the Gradle plugin:

```
export SECRET_PASSWORD=1234656
./gradlew test
``` 

### Standalone Execution
1. Create a PlantUML sequence diagram. Note the input requirements above. 
2. Save the sequence diagram. 
3. Call the command `./gradlew run --args="--input=<path/to/sequence/diagram/diagram_name.puml> --output <output_dir>"`.

The generated test cases are in `<path/to/sequence/diagram/generatedCode/<diagramName>.java>`.

## Integrated PlantUML IDE

Launch the integrated using `./gradlew jettyRun`. This launches a web-server on port 8080 and allows you to play with the
PlantUML grammar and render diagrams. **Note that the diagrams are sent to an external server which we do not control!**

## Used PlantUML Grammar

We created a new Grammar for PlantUML from scratch. The subproject is split into three parts:

* xyz.elite.xtext.languages.plantuml - Grammar and generators
* xyz.elite.xtext.languages.plantuml.ide - Currently unused IDE
* xyz.elite.xtext.languages.plantuml - Web IDE with live preview

The grammer supports all of the official PlantUml syntax.

## Limitations
- When actor A sends actor B a request, Plantestic expects actor B to send actor A a response. 
    Actors A and B must be different actors.
- Plantestic neither supports options nor loops.
- Plantestic can handle alternatives as long as they are not nested.
- We only support authenticated requests with username and password.

## Credits
### Contributors

2019:
- [Stefan Grafberger](https://github.com/stefan-grafberger) *
- [Fiona Guerin](https://github.com/FionaGuerin) *
- [Michelle Martin](https://github.com/MichelleMar) *
- [Daniela Neupert](https://github.com/danielaneupert) *
- [Andreas Zimmerer](https://github.com/Jibbow) *

\* contributed equally

2020
- Dominik
- Max
- Elias
- Alex

\* contributed equally

### Repositories
#### plantuml-eclipse-xtext
The repository [plantuml-eclipse-xtext](https://github.com/Cooperate-Project/plantuml-eclipse-xtext) defines the grammar of PlantUML. 
We pass this grammar to Xtext.
   
#### qvto-cli
The repository [qvto-cli](https://github.com/mrcalvin/qvto-cli) demonstrates how QVT operations can be performed without Eclipse.
   
### Literature
#### Standalone Parsing with Xtext
From the article [Standalone Parsing with Xtext](http://www.davehofmann.de/different-ways-of-parsing-with-xtext/) we learned how to use Xtext without Eclipse.
    
#### QVTOML/Examples/InvokeInJava
From the Wiki article [QVTOML/Examples/InvokeInJava](https://wiki.eclipse.org/QVTOML/Examples/InvokeInJava) we learned how to call QVT from our pipeline.

#### Grammar-based Program Generation Based on Model Finding
From the paper [Grammar-based Program Generation Based on Model Finding](http://www.informatik.uni-bremen.de/agra/doc/konf/13_idt_program_generation.pdf) we learned about the Eclipse Modeling Framework.
