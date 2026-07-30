package com.alertmesh.server

import java.util.Scanner
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("\u001B[36m╔══════════════════════════════════════╗")
    println("║  AlertMesh Rendezvous Server v0.1   ║")
    println("║  Topology-controlled frame relay    ║")
    println("╚══════════════════════════════════════╝\u001B[0m")
    
    val port = args.firstOrNull()?.toIntOrNull() ?: 9999
    
    val topologyManager = TopologyManager()
    val lossInjector = PacketLossInjector()
    val server = RendezvousServer(port, topologyManager, lossInjector)
    
    try {
        server.start()
    } catch (e: Exception) {
        println("\u001B[31mFATAL: Could not start server on port $port: ${e.message}\u001B[0m")
        exitProcess(1)
    }
    
    println("Type 'help' for commands.")
    
    val scanner = Scanner(System.`in`)
    while (true) {
        try {
            print("> ")
            if (!scanner.hasNextLine()) {
                println("\nNo interactive input detected. Server will continue running in background.")
                // Keep the process alive if the server is running
                Thread.currentThread().join()
                break
            }
            val line = scanner.nextLine()?.trim() ?: break
            if (line.isEmpty()) continue
            
            val parts = line.split("\\s+".toRegex())
            val cmd = parts[0].lowercase()
            
            when (cmd) {
                "help" -> {
                    println("Commands:")
                    println("  topology <line|ring|star|mesh> - Set topology for connected nodes")
                    println("  partition <nodeA,nodeB> <nodeC> - Partition network into two groups")
                    println("  heal                           - Restore pre-partition topology")
                    println("  loss <rate>                    - Set packet loss rate (0.0-1.0)")
                    println("  nodes                          - List connected nodes")
                    println("  status                         - Show current topology and loss rate")
                    println("  quit                           - Shutdown server")
                }
                "topology" -> {
                    val type = parts.getOrNull(1)?.lowercase()
                    val nodes = server.getConnectedNodes().sorted()
                    when (type) {
                        "line" -> { topologyManager.lineTopology(nodes); println("Topology set to LINE") }
                        "ring" -> { topologyManager.ringTopology(nodes); println("Topology set to RING") }
                        "star" -> { topologyManager.starTopology(nodes); println("Topology set to STAR") }
                        "mesh" -> { topologyManager.fullMeshTopology(nodes); println("Topology set to FULL MESH") }
                        else -> println("Unknown topology. Use line, ring, star, or mesh.")
                    }
                }
                "partition" -> {
                    if (parts.size >= 3) {
                        val group1 = parts[1].split(",").toSet()
                        val group2 = parts[2].split(",").toSet()
                        topologyManager.partition(group1, group2)
                        println("Partition created.")
                    } else {
                        println("Usage: partition <nodeA,nodeB> <nodeC>")
                    }
                }
                "heal" -> {
                    topologyManager.heal()
                    println("Network healed.")
                }
                "loss" -> {
                    val rate = parts.getOrNull(1)?.toDoubleOrNull()
                    if (rate != null) {
                        lossInjector.lossRate = rate
                        println("Packet loss rate set to $rate")
                    } else {
                        println("Usage: loss <rate>")
                    }
                }
                "nodes" -> {
                    val nodes = server.getConnectedNodes()
                    println("Connected nodes (${nodes.size}): ${nodes.joinToString()}")
                }
                "status" -> {
                    println("Packet Loss Rate: ${lossInjector.lossRate}")
                    val edges = topologyManager.getEdges()
                    println("Topology Edges: ${edges.joinToString { "${it.first}-${it.second}" }}")
                }
                "quit" -> {
                    server.stop()
                    println("Shutting down...")
                    exitProcess(0)
                }
                else -> println("Unknown command. Type 'help' for commands.")
            }
        } catch (e: NoSuchElementException) {
            println("\nInput stream closed. Server will continue running in background.")
            Thread.currentThread().join()
            break
        } catch (e: Exception) {
            println("\u001B[31mError executing command: ${e.message}\u001B[0m")
        }
    }
}
