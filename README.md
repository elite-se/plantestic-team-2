# Purpose

This project aims to build a Xtext definition of the PlantUML sequence diagram
language with some minor extensions, like builtin support for typed http request/response
message annotations.

# Limitations

Currently, this grammar has the following limitations:

- TEOZ PlantUML is not supported as of yet since it is still in alpha
- Only hexadecimal color specifications are supported, i.e., '#red' is not considered a valid color
- Descriptions (e.g. annotations on messages) may not contain ':', '...' or '==', unless those characters are escaped using double quotation marks ('"')
- inline HTML in notes is not support
- Multiline ref/title/note syntax of the form 'ref ... end ref' is not supported, please use \n to create multiline content

# Usage

## Setup

```
$ git clone <repo>
$ cd <repo>
$ ./gradlew jettyRun
```

Then open to the generated puml web code editor by opening `localhost:8080` in your browser.

## Test

```
$ ./gradlew test
```

## Debug Grammar

Firstly, you have to generate a debugable ANTLR Grammar file, by adding the `debugGrammer = true`
flag to `parserGenerator` settings in _GeneratePlantUML.mwe2_:

```
component = XtextGenerator {
  configuration = { ... }
  language = StandardLanguage {
    ...

    parserGenerator = {
      debugGrammar = true
    }
  }
}
```

Afterwards, the task `$ ./gradlew xyz.elite.xtext.languages.plantuml:generateXtext` will also generate
the file: _xyz.elite.xtext.languages.plantuml/src/main/xtext-gen/xyz/elite/xtext/languages/plantuml/parser/antlr/internal/DebugInternalPlantUML.g_.
You may load this file and debug it like you would any other ANTLR Grammar, e.g. by using [ANTLRWorks](https://www.antlr3.org/works/)
