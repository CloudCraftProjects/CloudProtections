plugins {
    id("java-library")
    id("maven-publish")

    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.booky"
version = "1.0.0"

val plugin: Configuration by configurations.creating { /**/ }

repositories {
    // TODO: find an actual repository for this
    mavenLocal {
        content {
            includeGroup("dev.booky")
        }
    }

    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") {
        content {
            includeGroup("dev.jorel")
        }
    }

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnlyApi("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    api("org.bstats:bstats-bukkit:3.0.2")

    // need to be published to maven local manually
    compileOnlyApi("dev.booky:cloudcore:1.0.0") {
        exclude("io.papermc.paper")
        exclude("org.bstats")
    }

    // testserver dependency plugins
    plugin("dev.booky:cloudcore:1.0.0:all") { isTransitive = false }
    plugin("dev.jorel:commandapi-bukkit-plugin:9.0.0-SNAPSHOT") { isTransitive = false }
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.cloudprotections.ProtectionsMain"
    apiVersion = "1.19"
    authors = listOf("booky10")
    depend = listOf("CloudCore")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    runServer {
        minecraftVersion("1.19.4")
        pluginJars.from(plugin.resolve())
    }

    shadowJar {
        relocate("org.bstats", "dev.booky.cloudprotections.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}