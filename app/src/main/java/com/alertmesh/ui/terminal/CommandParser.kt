package com.alertmesh.ui.terminal

object CommandParser {
    sealed class Command {
        object Help : Command()
        object Peers : Command()
        data class Ping(val target: String?) : Command()
        data class Trace(val target: String?) : Command()
        data class Msg(val target: String, val text: String) : Command()
        data class UniversalBroadcast(val text: String) : Command()
        data class Sos(val note: String? = null) : Command()
        object Fix : Command()
        data class Channel(val num: Int) : Command()
        object Sync : Command()
        object Status : Command()
        object Clear : Command()
        data class Unknown(val input: String) : Command()
    }

    fun parse(input: String): Command {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return Command.Unknown(input)

        // If no leading slash, treat whole input as Universal Broadcast
        if (!trimmed.startsWith("/")) {
            return Command.UniversalBroadcast(trimmed)
        }

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()

        return when (cmd) {
            "/help", "/?" -> Command.Help

            "/peers", "/nodes", "/list", "/l" -> Command.Peers

            "/ping", "/p" -> {
                val target = if (parts.size >= 2) parts[1] else null
                Command.Ping(target)
            }

            "/trace", "/tr" -> {
                val target = if (parts.size >= 2) parts[1] else null
                Command.Trace(target)
            }

            "/msg", "/send", "/broadcast", "/b", "/m" -> {
                if (parts.size == 1) {
                    Command.Unknown(input)
                } else if (parts.size == 2) {
                    // e.g. /msg Hello -> broadcast Hello
                    Command.UniversalBroadcast(parts[1])
                } else {
                    // e.g. /msg PeerA Hello world OR /msg Hello world team -> check target vs broadcast
                    val potentialTarget = parts[1]
                    val restText = parts.drop(2).joinToString(" ")
                    Command.Msg(potentialTarget, restText)
                }
            }

            "/sos", "/emergency" -> {
                val note = if (parts.size >= 2) parts.drop(1).joinToString(" ") else null
                Command.Sos(note)
            }

            "/fix", "/repair", "/rescan", "/restart" -> Command.Fix

            "/channel", "/ch", "/chan" -> {
                if (parts.size >= 2) {
                    val num = parts[1].toIntOrNull()
                    if (num != null && num in 1..8) Command.Channel(num) else Command.Unknown(input)
                } else {
                    Command.Unknown(input)
                }
            }

            "/sync" -> Command.Sync

            "/status", "/info", "/metrics", "/stat", "/sys" -> Command.Status

            "/clear", "/cls" -> Command.Clear

            else -> Command.Unknown(input)
        }
    }
}
