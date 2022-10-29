open class NestedModule(private val base: String) {
    fun module(name: String) = ":$base:$name"
}

object Modules {
    object Features: NestedModule("features") {
        val viewer = module("viewer")
        val shikimori = module("shikimori")
        val chapters = module("chapters")
        val library = module("library")
        val categories = module("categories")
        val statistic = module("statistic")
        val storage = module("storage")
        val settings = module("settings")
        val schedule = module("schedule")
    }

    object Core : NestedModule("core") {
        val support = module("support")
        val utils = module("utils")
        val compose = module("compose")
        val internet = module("internet")
        val download = module("download")
        val background = module("background")
    }

    object Data : NestedModule("data") {
        val db = module("db")
        val parsing = module("parsing")
        val models = module("models")
    }
}
