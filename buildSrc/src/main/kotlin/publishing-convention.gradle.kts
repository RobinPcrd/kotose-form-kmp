import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import java.util.Properties

plugins {
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    signing
}

val publishProperties = Properties().apply {
    load(file("publish.properties").inputStream())
}

val isGithubActions = System.getenv("GITHUB_ACTIONS") == "true"

// A release tag may carry a leading "v"; Maven and SwiftPM versions do not.
version = System.getenv("VERSION")?.removePrefix("v") ?: if (isGithubActions)
    error("VERSION must be set for GitHub Actions")
else
    "0.0.0-LOCAL"

val repositoryUrl = "https://github.com/$githubRepository"

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    coordinates(project.group as String, project.name, project.version as String)
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    // Maven Central requires signatures; local publications do not.
    if (providers.gradleProperty("signingInMemoryKey").isPresent)
        signAllPublications()

    pom {
        name.set(project.name)
        description.set(publishProperties.getProperty("description"))
        url.set(repositoryUrl)

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("robinpcrd")
                name.set("Robin Picard")
                email.set("robin.picard.dev@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/$githubRepository.git")
            developerConnection.set("scm:git:ssh://github.com/$githubRepository.git")
            url.set(repositoryUrl)
        }
    }
}
