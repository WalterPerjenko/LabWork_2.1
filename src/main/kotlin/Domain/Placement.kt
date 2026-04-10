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
}