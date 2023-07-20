plugins {
    id("java-library")
    id("maven-publish")

    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.booky"
version = "1.0.1"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://maven.pkg.github.com/CloudCraftProjects/*/") {
        name = "github"
        credentials(PasswordCredentials::class.java)
    }
    maven("https://repo.papermc.io/repository/maven-public/")
}

val cloudCoreVersion = "1.0.1-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.2")

    // need to be published to maven local manually
    compileOnlyApi("dev.booky:cloudcore:$cloudCoreVersion")

    // testserver dependency plugins
    plugin("dev.booky:cloudcore:$cloudCoreVersion:all")
    plugin("dev.jorel:commandapi-bukkit-plugin:9.0.3")
}

java {
    withSourcesJar()
    toolchain{
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.cloudprotections.ProtectionsMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    depend = listOf("CloudCore", "CommandAPI")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    runServer {
        minecraftVersion("1.20.1")
        pluginJars.from(plugin.resolve())
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.cloudprotections.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
