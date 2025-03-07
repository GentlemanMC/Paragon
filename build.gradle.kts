import org.spongepowered.asm.gradle.plugins.MixinExtension
import net.minecraftforge.gradle.userdev.UserDevExtension

buildscript {
    repositories {
        mavenCentral()

        maven("https://maven.minecraftforge.net/")
        maven("https://files.minecraftforge.net/maven/")
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:4.+")
        classpath("org.spongepowered:mixingradle:0.7.+")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
    }
}

plugins {
    java
    kotlin("jvm") version "1.7.0"
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
    plugin("kotlin")
    plugin("idea")
}

version = "1.0.0"
group = "com.paragon"

repositories {
    mavenCentral()

    maven("https://files.minecraftforge.net/maven/")
    maven("https://jitpack.io")
}

configure<UserDevExtension> {
    mappings(mapOf("channel" to "snapshot", "version" to "20171003-1.12"))

    runs {
        create("client") {
            workingDirectory = project.file("run").path

            properties(
                mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug",
                    "fml.coreMods.load" to "com.paragon.MixinLoader"
                )
            )
        }
    }
}

configurations.create("libraries")

dependencies {
    "minecraft"("net.minecraftforge:forge:1.12.2-14.23.5.2860")

    "libraries"("org.spongepowered:mixin:0.8") {
        exclude("module", "guava")
        exclude("module", "gson")
        exclude("module", "commons-io")
    }

    annotationProcessor("org.spongepowered:mixin:0.8:processor") {
        exclude("module", "gson")
    }

    "libraries"("org.json:json:20220924")
    "libraries"("com.github.Litarvan:OpenAuth:1.1.2")
    "libraries"("club.minnced:java-discord-rpc:2.0.1")
    "libraries"("com.github.therealbush:translator:1.0.2")
    "libraries"("com.github.Wolfsurge:animationsystem:6098d839c7")

    "libraries"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10") {
        exclude("module", "kotlin-stdlib-common")
        exclude("module", "annotations")
    }

    "libraries"("org.jetbrains.kotlin:kotlin-reflect:1.7.10") {
        exclude("module", "kotlin-stdlib")
    }

    "libraries"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") {
        exclude("module", "kotlin-stdlib-jdk8")
        exclude("module", "kotlin-stdlib-common")
    }

    implementation(configurations.getByName("libraries"))
}

configure<MixinExtension> {
    defaultObfuscationEnv = "searge"
    add(sourceSets["main"], "mixins.paragon.refmap.json")
    config("mixins.paragon.json")
}

tasks.processResources {
    from(project.the<SourceSetContainer>()["main"].resources.srcDirs) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        include("mcmod.info")
        expand(
            mapOf(
                "version" to version,
                "mcversion" to "1.12.2"
            )
        )
    }
}

tasks.jar {
    manifest.attributes(
        "Manifest-Version" to "1.0",
        "MixinConfigs" to "mixins.paragon.json",
        "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
        "FMLCorePluginContainsFMLMod" to "true",
        "FMLCorePlugin" to "com.paragon.MixinLoader",
        "ForceLoadAsMod" to "true"
    )

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.getByName("libraries").map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

project.the<SourceSetContainer>()["main"].java.srcDir("src/main/kotlin")

tasks.register("prepareAssets", Copy::class) {
    group = "paragon"

    from(project.file("src/main/resources"))
    into(project.file("build/classes/kotlin/main"))
}

tasks.register("buildApi", Jar::class) {
    group = "paragon"

    archiveClassifier.set("api")
    from(project.the<SourceSetContainer>()["main"].output)
}