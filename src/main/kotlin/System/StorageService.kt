package System

import Domain.*

class StorageService {

    private val containers = mutableMapOf<Long, Container>()
    private val slots = mutableMapOf<Long, Slot>()
    private val placements = mutableMapOf<Long, Placement>()

    private var containerSeq = 1L
    private var slotSeq = 1L
    private var placementSeq = 1L


    fun addContainer(name: String, type: ContainerType): Container {
        val c = Container(containerSeq++, name, type)
        containers[c.id] = c
        return c
    }

    fun getContainer(id: Long): Container =
        containers[id] ?: throw IllegalArgumentException("Контейнер не найден")

    fun getAllContainers(): List<Container> = containers.values.toList()

    // --- Slot ---
    fun createSlots(containerId: Long, rows: Int, cols: Int) {
        require(rows > 0 && cols > 0) { "Ошибка: rows/cols > 0" }

        for (i in 0 until rows) {
            val row = 'A' + i
            for (j in 1..cols) {
                val code = "$row$j"
                val slot = Slot(slotSeq++, containerId, code)
                slots[slot.id] = slot
            }
        }
    }

    fun getSlots(containerId: Long): List<Slot> =
        slots.values.filter { it.containerId == containerId }

    fun findSlot(containerId: Long, code: String): Slot =
        slots.values.find { it.containerId == containerId && it.code == code }
            ?: throw IllegalArgumentException("Слот не найден")

    // --- Placement ---
    fun place(sampleId: Long, containerId: Long, code: String) {
        val slot = findSlot(containerId, code)

        require(!slot.occupied) { "Ошибка: слот занят" }

        slot.occupied = true

        placements[sampleId] = Placement(
            placementSeq++, sampleId, containerId, slot.id
        )
    }

    fun move(sampleId: Long, containerId: Long, code: String) {
        val old = placements[sampleId]
            ?: throw IllegalArgumentException("Ошибка: не размещён")

        val newSlot = findSlot(containerId, code)
        require(!newSlot.occupied) { "Ошибка: слот занят" }

        slots[old.slotId]?.occupied = false
        newSlot.occupied = true

        placements[sampleId] =
            Placement(placementSeq++, sampleId, containerId, newSlot.id)
    }

    fun remove(sampleId: Long) {
        val p = placements.remove(sampleId)
            ?: throw IllegalArgumentException("Ошибка: не размещён")

        slots[p.slotId]?.occupied = false
    }

    fun find(sampleId: Long): Placement? = placements[sampleId]


    fun getMap(containerId: Long): String {
        val containerSlots = getSlots(containerId)

        val grouped = containerSlots.groupBy { it.code[0] }

        val sb = StringBuilder()
        for ((row, slots) in grouped) {
            sb.append("$row: ")
            for (s in slots.sortedBy { it.code }) {
                sb.append(if (s.occupied) "[X]" else "[ ]")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
