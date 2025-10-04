plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

android {
    namespace = "com.cairong.permission.coroutine"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        
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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // 依赖核心模块
    api(project(":permission-core"))
    
    // Android依赖
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // 协程依赖
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.test:core:1.5.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Dokka 配置
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    
    dokkaSourceSets {
        named("main") {
            moduleName.set("Permission Coroutine")
            moduleVersion.set("1.0.0")
            
            includes.from("Module.md")
            
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(uri("https://github.com/cairong/android-permission-framework/tree/main/permission-coroutine/src/main/java").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// 生成 Dokka JAR
val dokkaJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.cairong.permission"
            artifactId = "permission-coroutine"
            version = "1.0.0"
            
            afterEvaluate {
                from(components["release"])
            }
            
            // 添加文档 JAR
            artifact(dokkaJar)
            
            pom {
                name.set("Android Permission Framework Coroutine Extension")
                description.set("Kotlin Coroutine extensions for Android Permission Framework")
                url.set("https://github.com/cairong/android-permission-framework")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("cairong")
                        name.set("CaiRong")
                        email.set("cairong@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/cairong/android-permission-framework.git")
                    developerConnection.set("scm:git:ssh://github.com:cairong/android-permission-framework.git")
                    url.set("https://github.com/cairong/android-permission-framework/tree/main")
                }
            }
        }
    }
}