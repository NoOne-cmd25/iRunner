// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false      // ← 必须有
    id("org.jetbrains.kotlin.kapt") version "1.9.10" apply false
}