// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("android") version "1.9.0" apply false
    kotlin("plugin.compose") version "1.9.0" apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlin.compose) apply false
}