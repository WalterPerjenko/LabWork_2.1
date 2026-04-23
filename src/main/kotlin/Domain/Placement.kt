package Domain




import java.time.Instant

class Placement(
    val id: Long,
    val sampleId: Long,
    val containerId: Long,
    val slotId: Long,
    val ownerUsername: String = "SYSTEM"
) {
    val placedAt: Instant = Instant.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Placement) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}