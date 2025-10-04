plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

android {
    namespace = "com.cairong.permission"
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
    // 核心依赖：Activity Result API 和 Fragment
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Material Design（可选，用于BottomSheet支持）
    compileOnly("com.google.android.material:material:1.11.0")
    
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Dokka 配置
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    
    dokkaSourceSets {
        named("main") {
            moduleName.set("Permission Core")
            moduleVersion.set("1.0.0")
            
            includes.from("Module.md")
            
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(uri("https://github.com/cairong/android-permission-framework/tree/main/permission-core/src/main/java").toURL())
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
            artifactId = "permission-core"
            version = "1.0.0"
            
            afterEvaluate {
                from(components["release"])
            }
            
            // 添加文档 JAR
            artifact(dokkaJar)
            
            pom {
                name.set("Android Permission Framework Core")
                description.set("A zero-dependency, reusable, chainable Android permission request framework")
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