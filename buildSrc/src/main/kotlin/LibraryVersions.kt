import kotlin.reflect.full.memberProperties

object LibraryVersions {
    const val jackson = "2.10.2"
    const val hamkrest = "1.7.0.0"

    fun toMap() =
            LibraryVersions::class.memberProperties
                    .associate { prop -> prop.name to prop.getter.call() as String }
}
