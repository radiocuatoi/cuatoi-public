compileJava {
    options.compilerArgs << '-parameters'
}
task dockerClean(type: Exec, dependsOn: 'clean') {
    group = 'cuatoi'
    commandLine 'docker', 'system', 'prune', '-f'
}
