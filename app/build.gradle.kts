import com.android.build.api.dsl.ApplicationExtension

apply(plugin = "com.android.application")

extensions.configure<ApplicationExtension>("android") {
    namespace = "com.example.fitbook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitbook"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("implementation", "androidx.activity:activity-ktx:1.13.0")
    add("implementation", "androidx.appcompat:appcompat:1.7.1")
    add("implementation", "androidx.constraintlayout:constraintlayout:2.2.1")
    add("implementation", "com.google.android.material:material:1.13.0")
    add("testImplementation", "junit:junit:4.13.2")
    add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.7.0")
    add("androidTestImplementation", "androidx.test.ext:junit:1.3.0")
}
