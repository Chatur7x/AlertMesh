package com.alertmesh.server

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

class TopologyManager {
    private val edges = ConcurrentHashMap.newKeySet<Pair<String, String>>()
    private val prePartitionEdges = ConcurrentHashMap.newKeySet<Pair<String, String>>()
    private val _topologyChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val topologyChanges = _topologyChanges.asSharedFlow()

    fun areAdjacent(a: String, b: String): Boolean {
        return edges.contains(a to b) || edges.contains(b to a)
    }

    fun setTopology(newEdges: Set<Pair<String, String>>) {
        edges.clear()
        edges.addAll(newEdges)
        _topologyChanges.tryEmit(Unit)
    }

    fun getEdges(): Set<Pair<String, String>> = edges.toSet()

    fun getNeighbors(peerId: String): Set<String> {
        val neighbors = mutableSetOf<String>()
        edges.forEach { (a, b) ->
            if (a == peerId) neighbors.add(b)
            if (b == peerId) neighbors.add(a)
        }
        return neighbors
    }

    fun partition(group1: Set<String>, group2: Set<String>) {
        prePartitionEdges.clear()
        prePartitionEdges.addAll(edges)

        val newEdges = edges.filterNot { (a, b) ->
            (group1.contains(a) && group2.contains(b)) ||
            (group2.contains(a) && group1.contains(b))
        }.toSet()

        setTopology(newEdges)
    }

    fun heal() {
        if (prePartitionEdges.isNotEmpty()) {
            setTopology(prePartitionEdges.toSet())
            prePartitionEdges.clear()
        }
    }

    fun lineTopology(nodeIds: List<String>) {
        val newEdges = mutableSetOf<Pair<String, String>>()
        for (i in 0 until nodeIds.size - 1) {
            newEdges.add(nodeIds[i] to nodeIds[i + 1])
        }
        setTopology(newEdges)
    }

    fun ringTopology(nodeIds: List<String>) {
        val newEdges = mutableSetOf<Pair<String, String>>()
        if (nodeIds.size >= 2) {
            for (i in 0 until nodeIds.size - 1) {
                newEdges.add(nodeIds[i] to nodeIds[i + 1])
            }
            newEdges.add(nodeIds.last() to nodeIds.first())
        }
        setTopology(newEdges)
    }

    fun starTopology(nodeIds: List<String>) {
        val newEdges = mutableSetOf<Pair<String, String>>()
        if (nodeIds.size >= 2) {
            val center = nodeIds.first()
            for (i in 1 until nodeIds.size) {
                newEdges.add(center to nodeIds[i])
            }
        }
        setTopology(newEdges)
    }

    fun fullMeshTopology(nodeIds: List<String>) {
        val newEdges = mutableSetOf<Pair<String, String>>()
        for (i in nodeIds.indices) {
            for (j in i + 1 until nodeIds.size) {
                newEdges.add(nodeIds[i] to nodeIds[j])
            }
        }
        setTopology(newEdges)
    }
}
