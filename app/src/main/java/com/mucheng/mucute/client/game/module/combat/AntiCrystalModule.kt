package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.math.vector.Vector3f

class AntiCrystalModule : Module("anti_crystal", ModuleCategory.Combat) {

    private var ylevel by floatValue("ylevel", 0.4f, 0.1f..1.61f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is MovePlayerPacket) {
            // Server-side adjustment: Move the player down by ylevel
            packet.position = packet.position.add(0.0, -ylevel.toDouble(), 0.0)
        }
            // Client-side compensation: Send a packet to move the player up by ylevel
            val motionPacket = SetEntityMotionPacket()
            motionPacket.runtimeEntityId = session.localPlayer.runtimeEntityId
            motionPacket.motion = Vector3f.from(session.localPlayer.motionX.toDouble(), ylevel.toDouble(), session.localPlayer.motionZ.toDouble())
            session.clientBound(motionPacket)

    }


}