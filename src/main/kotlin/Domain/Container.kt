package Domain


import java.time.Instant

class Container(
    val id: Long,
    name: String,
    type: ContainerType,
    val ownerUsername: String = "SYSTEM"
) {

    var name: String = name
        set(value) {
            require(value.isNotBlank()) { "Ошибка: name пустой" }
            require(value.length <= 64) { "Ошибка: name слишком длинный" }
            field = value
            updatedAt = Instant.now()
        }

    var type: ContainerType = type
        set(value) {
            field = value ?: throw IllegalArgumentException("Ошибка: тип null")
            updatedAt = Instant.now()
        }

    val createdAt: Instant = Instant.now()
    var updatedAt: Instant = Instant.now()

    // 🔥 equals/hashCode по id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Container) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}