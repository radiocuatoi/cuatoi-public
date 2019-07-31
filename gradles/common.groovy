compileJava {
    options.compilerArgs << '-parameters'
}
task dockerClean(type: Exec, dependsOn: 'clean') {
    group = 'cuatoi'
    commandLine 'sh', '-c', 'docker system prune -f && docker rmi $(docker image list -q)'
}
