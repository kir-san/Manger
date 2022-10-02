open class NestedModule(private val base: String) {
    fun module(name: String) = ":$base:$name"
}

object Modules {
    object Features: NestedModule("features") {
        val viewer = module("viewer")
        val latest = module("latest")
        val shikimori = module("shikimori")
        val chapters = module("chapters")
        val library = module("library")
        val categories = module("categories")
    }

    object Core : NestedModule("core") {
        val support = module("support")
        val utils = module("utils")
        val composeUtils = module("compose_utils")
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
