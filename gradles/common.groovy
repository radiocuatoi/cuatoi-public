compileJava {
    options.compilerArgs << '-parameters'
}

task preCommitHooks(type: Exec) {
    group = 'cuatoi'
    doFirst {
        new File("$projectDir/.git/hooks/pre-commit").text = """#!/bin/sh
./gradlew clean build
"""
    }
    commandLine 'sh', '-c', "chmod +x $projectDir/.git/hooks/pre-commit"
}
init.dependsOn preCommitHooks
wrapper.dependsOn preCommitHooks

task dockerClean(type: Exec, dependsOn: 'clean') {
    group = 'cuatoi'
    commandLine 'sh', '-c', 'docker system prune -f && docker rmi $(docker image list -q)'
}
