buildscript {
    configurations.getByName("classpath").resolutionStrategy.eachDependency {
        val patchedVersion = when {
            requested.group == "io.netty" && requested.name.startsWith("netty-") &&
                !requested.name.startsWith("netty-tcnative") -> "4.1.138.Final"
            requested.group == "org.bouncycastle" -> "1.86"
            requested.group == "org.apache.commons" && requested.name == "commons-compress" -> "1.28.0"
            requested.group == "org.bitbucket.b_c" && requested.name == "jose4j" -> "0.9.7"
            requested.group == "org.jdom" && requested.name == "jdom2" -> "2.0.6.1"
            else -> null
        }
        if (patchedVersion != null) {
            useVersion(patchedVersion)
            because("Security fixes for Android build tool transitive dependencies")
        }
    }
}

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
