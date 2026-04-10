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
            require(value.isNotBlank()) { "Ошибка: name не может быть пустым" }
            require(value.length <= 64) { "Ошибка: name слишком длинный" }
            field = value
            updatedAt = Instant.now()
        }

    var type: ContainerType = type
        set(value) {
            field = value ?: throw IllegalArgumentException("Ошибка: неверный тип")
            updatedAt = Instant.now()
        }

    val createdAt: Instant = Instant.now()
    var updatedAt: Instant = Instant.now()
}