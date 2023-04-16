import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:${Version.Plugin.dokka}")
    }
}

plugins {
    kotlin("multiplatform") version Version.Plugin.kotlinMultiplatform
    kotlin("plugin.serialization") version Version.Plugin.kotlinSerialization
    id("org.jetbrains.dokka") version Version.Plugin.dokka
    id("maven-publish")
}

group = ProjectMetadata.GitHub.GroupId
version = ProjectMetadata.Build.Version

// region Kotlin Multiplatform (Common)

kotlin.sourceSets {
    commonMain.kotlin.srcDir("commonMain")
    commonTest.kotlin.srcDir("commonTest")

    commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.Dependency.kotlinSerialization}")
        implementation("io.github.rhdunn:xqt-kotlinx-json-rpc:${Version.Dependency.xqtJsonRpc}")
    }

    commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(project(":src:xqt-kotlinx-test"))
    }
}

// endregion
// region Kotlin JVM

kotlin.jvm {
    compilations.all {
        kotlinOptions.jvmTarget = BuildConfiguration.jvmTarget
    }

    withJava()

    testRuns["test"].executionTask.configure {
        useJUnitPlatform() // JUnit 5
    }
}

kotlin.sourceSets {
    jvmMain.kotlin.srcDir("jvmMain")
    jvmTest.kotlin.srcDir("jvmTest")
}

// endregion
// region Kotlin JS

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().download = BuildConfiguration.downloadNodeJs
}

kotlin.js(KotlinJsCompilerType.BOTH).browser {
    testTask {
        useKarma {
            when (BuildConfiguration.jsBrowser) {
                JsBrowser.Chrome -> useChromeHeadless()
                JsBrowser.ChromeCanary -> useChromeCanaryHeadless()
                JsBrowser.Chromium -> useChromiumHeadless()
                JsBrowser.Firefox -> useFirefoxHeadless()
                JsBrowser.FirefoxAurora -> useFirefoxAuroraHeadless()
                JsBrowser.FirefoxDeveloper -> useFirefoxDeveloperHeadless()
                JsBrowser.FirefoxNightly -> useFirefoxNightlyHeadless()
                JsBrowser.PhantomJs -> usePhantomJS()
                JsBrowser.Safari -> useSafari()
            }
        }
    }
}

kotlin.js(KotlinJsCompilerType.BOTH).nodejs {
}

kotlin.sourceSets {
    jsMain.kotlin.srcDir("jsMain")
    jsTest.kotlin.srcDir("jsTest")
}

// endregion
// region Kotlin Native

when (BuildConfiguration.hostOs) {
    HostOs.Windows -> kotlin.mingwX64("native")
    HostOs.Linux -> kotlin.linuxX64("native")
    HostOs.MacOsX -> kotlin.macosX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
}

kotlin.sourceSets {
    nativeMain.kotlin.srcDir("nativeMain")
    nativeTest.kotlin.srcDir("nativeTest")
}

// endregion
// region Documentation

tasks.withType<DokkaTask>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "Copyright © ${ProjectMetadata.Copyright.Year} ${ProjectMetadata.Copyright.Owner}"
    }
}

// endregion
