package System

import Domain.*

class StorageService {

    private val containers = mutableSetOf<Container>()
    private val slots = mutableSetOf<Slot>()
    private val placements = mutableSetOf<Placement>()

    private var containerSeq = 1L
    private var slotSeq = 1L
    private var placementSeq = 1L


    fun addContainer(name: String, type: ContainerType): Container {
        val c = Container(containerSeq++, name, type)
        containers.add(c)
        return c
    }

    fun getContainer(id: Long): Container =
        containers.find { it.id == id }
            ?: throw IllegalArgumentException("Контейнер не найден")

    fun getAllContainers(): List<Container> = containers.toList()


    fun createSlots(containerId: Long, rows: Int, cols: Int) {
        require(rows > 0 && cols > 0) { "Ошибка: rows/cols > 0" }

        for (i in 0 until rows) {
            val row = 'A' + i
            for (j in 1..cols) {
                val code = "$row$j"
                val slot = Slot(slotSeq++, containerId, code)
                slots.add(slot)
            }
        }
    }

    fun getSlots(containerId: Long): List<Slot> =
        slots.filter { it.containerId == containerId }

    fun findSlot(containerId: Long, code: String): Slot =
        slots.find { it.containerId == containerId && it.code == code }
            ?: throw IllegalArgumentException("Слот не найден")


    fun place(sampleId: Long, containerId: Long, code: String) {
        val slot = findSlot(containerId, code)
        require(!slot.occupied) { "Ошибка: слот занят" }

        slot.occupied = true

        val placement = Placement(
            placementSeq++, sampleId, containerId, slot.id
        )
        placements.add(placement)
    }

    fun move(sampleId: Long, containerId: Long, code: String) {
        val old = placements.find { it.sampleId == sampleId }
            ?: throw IllegalArgumentException("Ошибка: не размещён")

        val newSlot = findSlot(containerId, code)
        require(!newSlot.occupied) { "Ошибка: слот занят" }

        slots.find { it.id == old.slotId }?.occupied = false
        newSlot.occupied = true

        placements.remove(old)
        placements.add(
            Placement(placementSeq++, sampleId, containerId, newSlot.id)
        )
    }

    fun remove(sampleId: Long) {
        val p = placements.find { it.sampleId == sampleId }
            ?: throw IllegalArgumentException("Ошибка: не размещён")

        slots.find { it.id == p.slotId }?.occupied = false
        placements.remove(p)
    }

    fun find(sampleId: Long): Placement? =
        placements.find { it.sampleId == sampleId }


    fun getMap(containerId: Long): String {
        val containerSlots = getSlots(containerId)
        val grouped = containerSlots.groupBy { it.code[0] }

        val sb = StringBuilder()
        for ((row, rowSlots) in grouped) {
            sb.append("$row: ")
            for (s in rowSlots.sortedBy { it.code }) {
                sb.append(if (s.occupied) "[X]" else "[ ]")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}