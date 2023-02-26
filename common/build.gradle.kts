plugins {
    alias(libs.plugins.blossom)
}

repositories {
    maven("https://libraries.minecraft.net") {
        mavenContent {
            includeGroup("com.mojang")
        }
    }
    mavenCentral()
}

dependencies {
    implementation(projects.miniplaceholdersApi)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimesssage)
    compileOnly(libs.brigadier)
}

blossom{
    replaceToken("{version}", project.version)
    replaceTokenIn("src/main/java/me/dreamerzero/miniplaceholders/common/PluginConstants.java")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    jar {
        manifest {
            attributes("Automatic-Module-Name" to "me.dreamerzero.miniplaceholders.common")
        }
    }
}
