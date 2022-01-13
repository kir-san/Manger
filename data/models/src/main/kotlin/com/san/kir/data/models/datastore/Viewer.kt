package com.san.kir.data.models.datastore

data class Viewer(
    val orientation: Orientation,
    val cutOut: Boolean,
    val control: Control,
    val withoutSaveFiles: Boolean,
) {
    enum class Orientation(val number: Int) {
        PORT(0),
        PORT_REV(1),
        LAND(2),
        LAND_REV(3),
        AUTO(4),
        AUTO_PORT(5),
        AUTO_LAND(6),
    }

    data class Control(
        val taps: Boolean = false,
        val swipes: Boolean = false,
        val keys: Boolean = false,
    )
}
