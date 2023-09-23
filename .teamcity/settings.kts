import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.05"

project {

    vcsRoot(HttpsGithubComNordhofSpringPetclinicTeamcityDemoRefsHeadsMain1)

    buildType(Build)
    buildType(BuildContainer)
    buildType(DeployToK8s)

    features {
        githubIssues {
            id = "PROJECT_EXT_4"
            displayName = "nordhof/spring-petclinic-teamcity-demo"
            repositoryURL = "https://github.com/nordhof/spring-petclinic-teamcity-demo"
            authType = accessToken {
                accessToken = "credentialsJSON:eba846ff-180a-4e2a-8a57-c6a1d7929891"
            }
            param("tokenId", "")
        }
    }
}

object Build : BuildType({
    name = "Build"

    artifactRules = "target/spring-petclinic-*.jar"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
        dockerCommand {
            name = "Build Container"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = "ghcr.io/nordhof/spring-petclinic-teamcity-demo:%build.vcs.number%"
            }
        }
        dockerCommand {
            name = "Push Image"
            commandType = push {
                namesAndTags = "ghcr.io/nordhof/spring-petclinic-teamcity-demo:%build.vcs.number%"
            }
        }
    }

    features {
        perfmon {
        }
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = vcsRoot()
            }
        }
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_3"
            }
        }
        notifications {
            notifierSettings = emailNotifier {
                email = "weisgrab@nordhof.at"
            }
            buildFailed = true
            buildFinishedSuccessfully = true
            firstSuccessAfterFailure = true
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.arch", "aarch64")
    }
})

object BuildContainer : BuildType({
    name = "Build Container"

    vcs {
        root(HttpsGithubComNordhofSpringPetclinicTeamcityDemoRefsHeadsMain1)
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            name = "Build Container"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = "ghcr.io/nordhof/spring-petclinic-teamcity-demo:%build.vcs.number%"
            }
        }
        dockerCommand {
            name = "Push image"
            commandType = push {
                namesAndTags = "ghcr.io/nordhof/spring-petclinic-teamcity-demo:%build.vcs.number%"
            }
        }
    }

    features {
        perfmon {
        }
    }

    dependencies {
        artifacts(Build) {
            buildRule = lastSuccessful()
            artifactRules = "spring-petclinic-*-SNAPSHOT.jar"
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.arch", "aarch64")
    }
})

object DeployToK8s : BuildType({
    name = "Deploy to k8s"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "Deploy"
            scriptContent = """
                echo "%k8s-config%" > kube-config
                export IMAGE_ID="%build.vcs.number%"
                ./k8s/deploy.sh
                rm kube-config
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }

    dependencies {
        snapshot(Build) {
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.arch", "aarch64")
    }
})

object HttpsGithubComNordhofSpringPetclinicTeamcityDemoRefsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/nordhof/spring-petclinic-teamcity-demo#refs/heads/main (1)"
    url = "https://github.com/nordhof/spring-petclinic-teamcity-demo"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "Micky002"
        password = "credentialsJSON:9601c217-cb59-4ca0-ab40-924d0ff6dc7a"
    }
})
