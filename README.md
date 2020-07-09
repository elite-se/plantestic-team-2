# Setup

```
$ git clone <repo>
$ cd <repo>
$ ./gradlew jettyRun
```

Then open to the generated puml web code editor by opening `localhost:8080` in your browser.

# Test PUML

```
SEQUENCE @startuml

participant "I have a really long\nlong name" as L #99FF99
participant Alice #red
actor FOO

FOO <- BAR : POST "/lol/xd/${var}" (var : "1")
... wait(10 d) : Dies ist ein Kommentar ...
BAR --> FOO : 200, 201, 410 - (res: "42")
participant CCC-Mock

@enduml
```
