package com.mucheng.mucute.client.game.world

import com.mucheng.mucute.client.game.GameSession
import com.mucheng.mucute.client.game.entity.Entity
import com.mucheng.mucute.client.game.entity.EntityUnknown
import com.mucheng.mucute.client.game.entity.Item
import com.mucheng.mucute.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

@Suppress("MemberVisibilityCanBePrivate")
class Level(val session: GameSession) {

    val entityMap = ConcurrentHashMap<Long, Entity>()
    val playerMap = ConcurrentHashMap<UUID, PlayerListPacket.Entry>()

    fun onDisconnect() {
        entityMap.clear()
        playerMap.clear()
    }

    fun onPacketBound(packet: BedrockPacket) {
        when (packet) {
            is StartGamePacket -> {
                entityMap.clear()
                playerMap.clear()
            }

            is AddEntityPacket -> {
                val entity = EntityUnknown(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.identifier
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                    handleSetAttribute(packet.attributes)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddItemEntityPacket -> {
                val entity = Item(packet.runtimeEntityId, packet.uniqueEntityId).apply {
                    move(packet.position)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddPlayerPacket -> {
                val entity = Player(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.uuid,
                    packet.username
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is RemoveEntityPacket -> {
                val entityToRemove =
                    entityMap.values.find { it.uniqueEntityId == packet.uniqueEntityId } ?: return
                entityMap.remove(entityToRemove.runtimeEntityId)
            }

            is TakeItemEntityPacket -> {
                entityMap.remove(packet.itemRuntimeEntityId)
            }

            is PlayerListPacket -> {
                val add = packet.action == PlayerListPacket.Action.ADD
                packet.entries.forEach {
                    if (add) {
                        playerMap[it.uuid] = it
                    } else {
                        playerMap.remove(it.uuid)
                    }
                }
            }

            else -> {
                entityMap.values.forEach { entity ->
                    entity.onPacketBound(packet)
                }
            }
        }
    }

    /**
     * Simulates explosion damage for entities within the explosion radius.
     *
     * @param center The center of the explosion.
     * @param size The radius of the explosion.
     * @param extraEntities Additional entities to consider for damage (e.g., custom entities).
     * @param damageCallback A callback function that receives the entity and the calculated damage.
     */
    fun simulateExplosionDamage(center: Vector3f, size: Float, extraEntities: List<Entity>, damageCallback: (Entity, Float) -> Unit) {
        val explosionSearchSizeSq = (size * 2).pow(2)

        // Check entities in the entityMap
        entityMap.values.filter { it.distanceSq(center) < explosionSearchSizeSq }.forEach { entity ->
            val distance = entity.distance(center) / size

            if (distance <= 1) {
                val impact = 1 - distance
                val damage = ((impact * impact + impact) / 2) * 8 * size + 1
                damageCallback(entity, damage)
            }
        }

        // Check extra entities
        extraEntities.filter { it.distanceSq(center) < explosionSearchSizeSq }.forEach { entity ->
            val distance = entity.distance(center) / size

            if (distance <= 1) {
                val impact = 1 - distance
                val damage = ((impact * impact + impact) / 2) * 8 * size + 1
                damageCallback(entity, damage)
            }
        }
    }
}