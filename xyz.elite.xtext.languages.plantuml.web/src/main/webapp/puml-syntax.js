define([], function() {
    var keywords = "startconfig|endconfig|async|startuml|enduml|SEQUENCE|activate|actor|alt|as|autoactivate|autonumber|boundary|box|break|collections|control|create|critical|database|deactivate|destroy|else|end|entity|footbox|footer|group|header|hide|hnote|left|loop|newpage|note|of|off|on|opt|order|over|par|participant|ref|resume|return|right|rnote|skinparam|stop|title";
    return {
        id: "xtext.puml",
        contentTypes: ["xtext/puml"],
        patterns: [
            {include: "orion.lib#brace_open"},
            {include: "orion.lib#brace_close"},
            {include: "orion.lib#bracket_open"},
            {include: "orion.lib#bracket_close"},
            {include: "orion.lib#parenthesis_open"},
            {include: "orion.lib#parenthesis_close"},
            {include: "orion.lib#string_doubleQuote"},
            {name: "keyword.puml", match: "\\b(?:" + keywords + ")\\b"},
            {name: "constant.language.puml", match: "\\b(DELETE|GET|PATCH|POST|PUT)\\b"},
            {name: "comment.line.quote.puml", match: "' .*"},
            {name: "comment.block.puml", begin: "/'", end: "'/"},
            {name: "constant.numeric.puml", match: "#([0-9]|[a-f]|[A-F])([0-9]|[a-f]|[A-F])([0-9]|[a-f]|[A-F])(([0-9]|[a-f]|[A-F])([0-9]|[a-f]|[A-F])([0-9]|[a-f]|[A-F]))?\\b"},
            {name: "constant.numeric.puml", match: "\\b\\d+\\s*(ns|ms|s|min|h|d)?\\b"},
            {name: "support.function.puml", match: "\\b(request|response)\\b"},
        ]
    };
});
