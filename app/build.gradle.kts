plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace="com.dualrobotics.maxcontrol"; compileSdk=35; compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }; defaultConfig { applicationId="com.dualrobotics.maxcontrol.v2"; minSdk=26; targetSdk=35; versionCode=1; versionName="1.0.0" } }
dependencies { implementation("androidx.core:core-ktx:1.13.1"); implementation("androidx.activity:activity-ktx:1.9.2"); implementation("com.google.android.material:material:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0") }


kotlin { jvmToolchain(17) }
