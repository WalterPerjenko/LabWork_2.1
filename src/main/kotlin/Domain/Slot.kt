package Domain

import java.time.Instant

class Slot(
    val id: Long,
    val containerId: Long,
    code: String
) {

    var code: String = code
        set(value) {
            require(value.isNotBlank()) { "Ошибка: code пустой" }
            require(value.length <= 8) { "Ошибка: code слишком длинный" }
            field = value
        }

    var occupied: Boolean = false
    val createdAt: Instant = Instant.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Slot) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}