plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.runpaper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "1.0.4-SNAPSHOT"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paperapi)

    compileOnlyApi(libs.cloudcore)
    implementation(libs.bstats)

    // testserver dependency plugins
    plugin(variantOf(libs.cloudcore) { classifier("all") })
    plugin(libs.commandapi.plugin)
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
}
