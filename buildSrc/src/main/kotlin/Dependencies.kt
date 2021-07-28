object Dependencies {
    object Kotlin {
        const val STDLIB =
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.Kotlin.STDLIB}"

        const val COROUTINES_CORE =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.COROUTINES}"
        const val COROUTINES_ANDROID =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.COROUTINES}"
    }

    object AndroidX {

        object Room {
            const val ROOM_RUNTIME =
                "androidx.room:room-runtime:${Versions.AndroidX.ROOM}"
            const val ROOM_COMPILER =
                "androidx.room:room-compiler:${Versions.AndroidX.ROOM}"
            const val ROOM_KTX =
                "androidx.room:room-ktx:${Versions.AndroidX.ROOM}"
        }

        object Lifecycle {
            const val LIFECYCLE_VIEWMODEL =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.LIFECYCLE}"
            const val LIFECYCLE_RUNTIME =
                "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.LIFECYCLE}"
            const val LIFECYCLE_LIVEDATA =
                "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.LIFECYCLE}"
            const val LIFECYCLE_COMMON =
                "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.LIFECYCLE}"
            const val LIFECYCLE_PROCESS =
                "androidx.lifecycle:lifecycle-process:${Versions.AndroidX.LIFECYCLE}"
            const val LIFECYCLE_SERVICE =
                "androidx.lifecycle:lifecycle-service:${Versions.AndroidX.LIFECYCLE}"
        }

        object WorkManager {
            const val WORK_RUNTIME =
                "androidx.work:work-runtime-ktx:${Versions.AndroidX.WORKMANAGER}"
            const val WORK_GCM =
                "androidx.work:work-gcm:${Versions.AndroidX.WORKMANAGER}"
            const val WORK_MULTIPROCESS =
                "androidx.work:work-multiprocess:${Versions.AndroidX.WORKMANAGER}"
        }

        object Datastore {
            const val DATASTORE =
                "androidx.datastore:datastore:${Versions.AndroidX.DATASTORE}"
        }

        const val CORE =
            "androidx.core:core-ktx:${Versions.AndroidX.CORE}"
        const val APPCOMPAT =
            "androidx.appcompat:appcompat:${Versions.AndroidX.APPCOMPAT}"
        const val COLLECTION =
            "androidx.collection:collection-ktx:${Versions.AndroidX.COLLECTION}"
        const val PREFERENCE =
            "androidx.preference:preference-ktx:${Versions.AndroidX.PREFERENCE}"
        const val PAGING =
            "androidx.paging:paging-runtime-ktx:${Versions.AndroidX.PAGING}"
        const val VECTORDRAWABLE =
            "androidx.vectordrawable:vectordrawable:${Versions.AndroidX.VECTORDRAWABLE}"
        const val CONSTRAINTLAYOUT =
            "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.CONSTRAINTLAYOUT}"
    }

    object Google {
        object Accompanist {
            const val FLOWLAYOUT =
                "com.google.accompanist:accompanist-flowlayout:${Versions.Google.ACCOMPANIST}"
            const val PAGER =
                "com.google.accompanist:accompanist-pager:${Versions.Google.ACCOMPANIST}"
            const val PAGER_INDICATORS =
                "com.google.accompanist:accompanist-pager-indicators:${Versions.Google.ACCOMPANIST}"
            const val PERMISSIONS =
                "com.google.accompanist:accompanist-permissions:${Versions.Google.ACCOMPANIST}"
            const val SYSTEMUICONTROLLER =
                "com.google.accompanist:accompanist-systemuicontroller:${Versions.Google.ACCOMPANIST}"
            const val INSETS =
                "com.google.accompanist:accompanist-insets:${Versions.Google.ACCOMPANIST}"
            const val INSETS_UI =
                "com.google.accompanist:accompanist-insets-ui:${Versions.Google.ACCOMPANIST}"
        }

        object Hilt {
            const val HILT_ANDROID =
                "com.google.dagger:hilt-android:${Versions.AndroidX.HILT}"
            const val HILT_COMPILER =
                "com.google.dagger:hilt-compiler:${Versions.AndroidX.HILT}"
        }

        const val PROTOBUF =
            "com.google.protobuf:protobuf-javalite:${Versions.Google.PROTOBUF_JAVALITE}"
        const val MATERIAL =
            "com.google.android.material:material:${Versions.Google.MATERIAL}"
        const val PLAY_SERVICES_GCM =
            "com.google.android.gms:play-services-gcm:${Versions.Google.PLAY_SERVICES_GCM}"
    }

    object Kittinunf {
        const val FUEL =
            "com.github.kittinunf.fuel:fuel-coroutines:${Versions.Kittinunf.FUEL}"
        const val RESULT =
            "com.github.kittinunf.result:result:${Versions.Kittinunf.RESULT}"
        const val RESULT_COROUTINES =
            "com.github.kittinunf.result:result-coroutines:${Versions.Kittinunf.RESULT}"
    }

    object Other {
        const val JSOUP =
            "org.jsoup:jsoup:${Versions.JSOUP}"
        const val ANDROID_JOB =
            "com.evernote:android-job:${Versions.ANDROID_JOB}"
        const val PROGRESSBUTTON =
            "com.github.razir.progressbutton:progressbutton:${Versions.PROGRESSBUTTON}"

    }

    object Compose {
        const val UI =
            "androidx.compose.ui:ui:${Versions.Compose.COMPOSE}"
        const val RUNTIME =
            "androidx.compose.runtime:runtime:${Versions.Compose.COMPOSE}"
        const val COMPILER =
            "androidx.compose.compiler:compiler:${Versions.Compose.COMPOSE}"
        const val ANIMATION =
            "androidx.compose.animation:animation:${Versions.Compose.COMPOSE}"
        const val FOUNDATION =
            "androidx.compose.foundation:foundation:${Versions.Compose.COMPOSE}"
        const val FOUNDATION_LAYOUT =
            "androidx.compose.foundation:foundation-layout:${Versions.Compose.COMPOSE}"
        const val MATERIAL = "androidx.compose.material:material:${Versions.Compose.COMPOSE}"
        const val MATERIAL_ICONS_CORE =
            "androidx.compose.material:material-icons-core:${Versions.Compose.COMPOSE}"
        const val MATERIAL_ICONS_EXTENDED =
            "androidx.compose.material:material-icons-extended:${Versions.Compose.COMPOSE}"

        const val ACTIVITY =
            "androidx.activity:activity-compose:${Versions.Compose.ACTIVITY_COMPOSE}"
        const val NAVIGATION =
            "androidx.navigation:navigation-compose:${Versions.Compose.NAVIGATION_COMPOSE}"
        const val HILT_NAVIGATION =
            "androidx.hilt:hilt-navigation-compose:${Versions.Compose.HILT_NAVIGATION_COMPOSE}"
    }

}

