package com.alertmesh.protocol

import com.alertmesh.protocol.model.Condition
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.protocol.routing.DistanceEstimator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SosPayloadAndHeightTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun automaticSosKeepsConditionUnknownAcrossWire() {
        val sos = SosPayload(1, 0, Condition.UNKNOWN, "Automatic SOS; details not provided")
        val decoded = ProtoBuf.decodeFromByteArray(SosPayload.serializer(),
            ProtoBuf.encodeToByteArray(SosPayload.serializer(), sos))
        assertEquals(Condition.UNKNOWN, decoded.condition)
        assertEquals(sos.note, decoded.note)
    }

    @Test
    fun lowerPressureMeansHigherRelativeElevation() {
        assertEquals(0.0, DistanceEstimator.barometricHeight(1000f, 1000f), 0.001)
        assertTrue(DistanceEstimator.barometricHeight(1000f, 999f) > 0.0)
    }
}
