import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PublishModTask

plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.runpaper)
    alias(libs.plugins.shadow)
    alias(libs.plugins.publishing)
}

group = "dev.booky"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paperapi)

    compileOnlyApi(libs.cloudcore)
    compileOnlyApi(libs.commandapi.bukkit.core)
    implementation(libs.bstats)

    // testserver dependency plugins
    plugin(variantOf(libs.cloudcore) { classifier("all") })
    plugin(libs.commandapi.bukkit.plugin)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
    repositories.maven("https://repo.cloudcraftmc.de/releases") {
        name = "horreo"
        credentials(PasswordCredentials::class.java)
    }
}

bukkit {
    main = "$group.cloudprotections.ProtectionsMain"
    apiVersion = "1.20.5"
    authors = listOf("booky10")
    website = "https://github.com/CloudCraftProjects/CloudProtections"
    depend = listOf("CloudCore", "CommandAPI")
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        pluginJars.from(plugin.resolve())
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.cloudprotections.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }

    withType<Jar> {
        manifest.attributes(
            "paperweight-mappings-namespace" to "mojang"
        )
    }
}

configure<ModPublishExtension> {
    val repositoryName = "CloudCraftProjects/CloudProtections"
    changelog = "See https://github.com/$repositoryName/releases/tag/v${project.version}"
    type = if (project.version.toString().endsWith("-SNAPSHOT")) BETA else STABLE
    dryRun = !hasProperty("noDryPublish")

    file = tasks.named<Jar>("shadowJar").flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar"))

    github {
        accessToken = providers.environmentVariable("GITHUB_API_TOKEN")
            .orElse(providers.gradleProperty("ccGithubToken"))

        displayName = "${rootProject.name} v${project.version}"

        repository = repositoryName
        commitish = "master"
        tagName = "v${project.version}"

        if (project != rootProject) {
            parent(rootProject.tasks.named("publishGithub"))
        }
    }
    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_TOKEN")
            .orElse(providers.gradleProperty("ccModrinthToken"))

        version = "${project.version}"
        displayName = "${rootProject.name} v${project.version}"
        modLoaders.add("paper")

        projectId = "IeP9peMi"
        minecraftVersionRange {
            start = rootProject.libs.versions.paperapi.get().split("-")[0]
            end = "latest"
        }

        requires("commandapi", "cloudcore")
    }
}

tasks.withType<PublishModTask> {
    dependsOn(tasks.named<Jar>("shadowJar"))
    dependsOn(tasks.named<Jar>("sourcesJar"))
}
