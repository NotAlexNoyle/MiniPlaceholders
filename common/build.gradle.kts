plugins {
    id("miniplaceholders.auto.module")
    alias(libs.plugins.blossom)
}

dependencies {
    implementation(projects.miniplaceholdersApi)
    implementation(libs.desertwell) {
        exclude("org.json")
    }
    implementation(libs.cloud.core)
    implementation(libs.cloud.extras) {
        isTransitive = false
    }
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimesssage)
}

blossom {
    replaceToken("{version}", project.version)
    replaceTokenIn("src/main/java/io/github/miniplaceholders/common/PluginConstants.java")
}
