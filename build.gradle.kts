plugins {
    commons
    packaging
    releasing
    detekt
    id("org.jetbrains.dokka") apply false
    id("com.github.johnrengelman.shadow") apply false
    id("com.github.ben-manes.versions")
    id("org.sonarqube")
    id("com.autonomousapps.dependency-analysis") version "0.56.0"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
