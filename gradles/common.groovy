compileJava {
    options.compilerArgs << '-parameters'
}

task dockerClean(type: Exec, dependsOn: 'clean') {
    group = 'cuatoi'
    commandLine 'sh', '-c', 'docker system prune -f && docker rmi $(docker image list -q)'
}

//set up git hooks
def preCommitFile = new File("$projectDir/.git/hooks/pre-commit.sh")
preCommitFile.text = """#!/bin/sh
./gradlew clean build
"""
preCommitFile.setExecutable(true, false)