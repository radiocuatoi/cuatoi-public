println '''To enable GAE support:
- add googleAppEngine("projectName")
- add the following config:
buildscript {
    dependencies {
        classpath "com.google.cloud.tools:appengine-gradle-plugin:2.0.1"
    }
}
'''
ext.googleAppEngine = { gaeProject ->
    println "Enabling GAE with project ${gaeProject}"
    apply plugin: 'com.google.cloud.tools.appengine'
    appengineDeploy.dependsOn test
    appengineStage.dependsOn test

    dependencies {
        providedRuntime('org.springframework.boot:spring-boot-starter-tomcat')
    }

    appengine {
        deploy {
            projectId = gaeProject
            stopPreviousVersion = true
            promote = true
            version = '1'
        }
    }

    task appengineDockerBuild(type: Exec, dependsOn: 'build') {
        group = 'cuatoi'
        doFirst {
            new File("$buildDir/Dockerfile").text = """FROM google/cloud-sdk:alpine
RUN apk --no-cache add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN apk --no-cache add nss
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
ENV PATH \$PATH:\$JAVA_HOME/jre/bin:\$JAVA_HOME/bin
RUN gcloud components install app-engine-java
RUN java -version && javac -version
WORKDIR /app/

ADD . /app/
"""
        }
        commandLine 'docker', "build", "--pull", "-t", "$gaeProject:dev", "-f", "$buildDir/Dockerfile", "."
    }
    task appengineDockerRun(type: Exec, dependsOn: 'appengineDockerBuild') {
        group = 'cuatoi'
        commandLine "docker", "run", "--rm", "--network=host", "--sig-proxy=true",
                "-v=gcloud-java-gradle:/root/.gradle", "--name=$gaeProject-dev", "$gaeProject:dev",
                "./gradlew", "clean", "appengineRun"
    }
    task appengineDockerDeploy(type: Exec, dependsOn: 'appengineDockerBuild') {
        group = 'cuatoi'
        doFirst {
            new File("$buildDir/deploy.sh").text = "gcloud auth activate-service-account --key-file=src/main/appengine/deploy.json &&" +
                    "./gradlew clean appengineDeploy"
        }
        commandLine 'docker', "run", "--rm", "--network=host", "--sig-proxy=true",
                "-v=gcloud-java-gradle:/root/.gradle",
                "-v=$buildDir/deploy.sh:/app/deploy.sh",
                "--name=$gaeProject-dev", "$gaeProject:dev",
                "sh", "./deploy.sh"
    }
    task appengineDockerDeployCron(type: Exec, dependsOn: 'appengineDockerBuild') {
        group = 'cuatoi'
        doFirst {
            new File("$buildDir/deploy.sh").text = "gcloud auth activate-service-account --key-file=src/main/appengine/deploy.json &&" +
                    "./gradlew clean appengineDeployCron"
        }
        commandLine 'docker', "run", "--rm", "--network=host", "--sig-proxy=true",
                "-v=gcloud-java-gradle:/root/.gradle",
                "-v=$buildDir/deploy.sh:/app/deploy.sh",
                "--name=$gaeProject-dev", "$gaeProject:dev",
                "sh", "./deploy.sh"
    }
}