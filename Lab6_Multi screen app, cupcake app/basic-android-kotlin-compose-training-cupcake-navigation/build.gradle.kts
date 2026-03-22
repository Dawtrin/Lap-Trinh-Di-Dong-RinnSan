buildscript {
    extra.apply {
        set("lifecycle_version", "2.8.7")
    }
}
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
}