import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "com.ruyomi.dev.utils.rexfile"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true    // 开启BuildConfig类的生成
        aidl = true           // 启用aidl
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets["main"].java.srcDir("src/main/java")
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
}

dependencies {

    // Shizuku
    api(libs.dev.api)
    api(libs.dev.provider)
    // DocumentFile
    api(libs.androidx.documentfile)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("reflect"))
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    allprojects.forEach { project ->
        project.afterEvaluate {
            project.extensions.findByType(PublishingExtension::class.java)?.apply {
                project.extensions.findByType(SigningExtension::class.java)?.apply {
                    useGpgCmd()
                    publishing.publications.withType(MavenPublication::class.java)
                        .forEach { publication ->
                            sign(publication)
                        }
                }
            }
        }
    }

    coordinates(
        groupId = "com.ruyomi.dev.utils",
        artifactId = "rex-file",
        version = libs.versions.rexFile.get()
    )

    pom {
        name.set("rex-file-android")
        description.set("A file operation library suitable for Android platform.")
        url.set("https://github.com/Ruyomi/RexFile")

        licenses {
            license {
                name.set("GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
            }
        }

        developers {
            developer {
                name.set("Ruyomi")
                email.set("mingyubmy@qq.com")
                url.set("https://github.com/Ruyomi")
            }
        }

        scm {
            url.set("https://github.com/Ruyomi/RexFile")
            connection.set("scm:git@github.com/Ruyomi/RexFile.git")
            developerConnection.set("scm:git@github.com/Ruyomi/RexFile.git")
        }
    }
}