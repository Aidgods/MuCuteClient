package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class BhopModule : Module("bhop", ModuleCategory.Motion) {

    private val jumpHeight by floatValue("jumpHeight", 0.42f, 0.4f..3.0f)
    private val motionInterval by intValue("motionInterval", 120, 50..2000)
    private var lastMotionTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (!isEnabled) {
            return
        }

        val currentTime = System.currentTimeMillis()

        if (packet is PlayerAuthInputPacket) {
            // Check if the player has hit the ground
            if (packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {
                // Apply upward motion immediately when the player hits the ground
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        session.localPlayer.motionX,  // Keep horizontal motion
                        jumpHeight,  // Apply upward motion
                        session.localPlayer.motionZ   // Keep horizontal motion
                    )
                }

                // Send the motion packet to the server
                session.clientBound(motionPacket)

                // Update the last motion time
                lastMotionTime = currentTime
            }
        }
    }
}