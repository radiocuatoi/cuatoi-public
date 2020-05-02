compileJava {
    options.compilerArgs << '-parameters'
}

test {
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
    }
}

//set up git hooks
def preCommitFile = new File("$projectDir/.git/hooks/pre-commit")
preCommitFile.text = """#!/bin/sh
./gradlew clean build
"""
preCommitFile.setExecutable(true, false)