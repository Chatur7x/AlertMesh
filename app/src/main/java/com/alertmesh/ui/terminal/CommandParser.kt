package com.alertmesh.ui.terminal

object CommandParser {
    sealed class Command {
        object Help : Command()
        object Peers : Command()
        data class Ping(val target: String) : Command()
        data class Trace(val target: String) : Command()
        data class Msg(val target: String, val text: String) : Command()
        object Sos : Command()
        object Fix : Command()
        data class Channel(val num: Int) : Command()
        object Sync : Command()
        object Metrics : Command()
        object Clear : Command()
        data class Unknown(val input: String) : Command()
    }

    fun parse(input: String): Command {
        val trimmed = input.trim()
        if (!trimmed.startsWith("/")) return Command.Unknown(input)
        
        val parts = trimmed.split("\\s+".toRegex())
        if (parts.isEmpty()) return Command.Unknown(input)

        val cmd = parts[0].lowercase()
        return when (cmd) {
            "/help" -> Command.Help
            "/peers" -> Command.Peers
            "/ping" -> {
                if (parts.size >= 2) Command.Ping(parts[1]) else Command.Unknown(input)
            }
            "/trace" -> {
                if (parts.size >= 2) Command.Trace(parts[1]) else Command.Unknown(input)
            }
            "/msg" -> {
                if (parts.size >= 3) {
                    val text = parts.drop(2).joinToString(" ")
                    Command.Msg(parts[1], text)
                } else {
                    Command.Unknown(input)
                }
            }
            "/sos" -> Command.Sos
            "/fix" -> Command.Fix
            "/channel" -> {
                if (parts.size >= 2) {
                    val num = parts[1].toIntOrNull()
                    if (num != null && num in 1..8) Command.Channel(num) else Command.Unknown(input)
                } else {
                    Command.Unknown(input)
                }
            }
            "/sync" -> Command.Sync
            "/metrics" -> Command.Metrics
            "/clear" -> Command.Clear
            else -> Command.Unknown(input)
        }
    }
}
