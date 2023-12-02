plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.runpaper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "1.0.3-SNAPSHOT"

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
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
    repositories.maven("https://maven.pkg.github.com/CloudCraftProjects/CloudProtections") {
        name = "github"
        credentials(PasswordCredentials::class.java)
    }
}

bukkit {
    main = "$group.cloudprotections.ProtectionsMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    website = "https://github.com/CloudCraftProjects/CloudProtections"
    depend = listOf("CloudCore", "CommandAPI")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
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
