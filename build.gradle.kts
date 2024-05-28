import sp.gx.core.buildDir
import sp.gx.core.buildSrc

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
        classpath("com.android.tools.build:gradle:8.2.2")
    }
}

task<Delete>("clean") {
    delete = setOf(buildDir(), buildSrc.buildDir())
}
