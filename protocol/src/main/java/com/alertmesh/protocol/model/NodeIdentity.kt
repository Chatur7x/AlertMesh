package com.alertmesh.protocol.model

import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class NodeIdentity(
    val deviceId: String,
    val publicKey: ByteArray,
    val fingerprint: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NodeIdentity) return false
        if (deviceId != other.deviceId) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (fingerprint != other.fingerprint) return false
        return true
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        return result
    }

    companion object {
        private val WORDS = listOf(
            "apple", "brave", "crane", "delta", "eagle", "flame", "grape", "heart",
            "igloo", "jelly", "karma", "lemon", "mango", "noble", "ocean", "pearl",
            "queen", "river", "stone", "tiger", "umbra", "venom", "water", "xenon",
            "yacht", "zebra", "amber", "baker", "cider", "dance", "ember", "frost",
            "globe", "honey", "ivory", "joker", "koala", "lunar", "magic", "ninja",
            "oasis", "panda", "quark", "raven", "solar", "tulip", "unity", "viper",
            "wheat", "x-ray", "youth", "zesty", "acorn", "bison", "coral", "diner",
            "essay", "fairy", "ghost", "hazel", "index", "jewel", "kiosk", "laser",
            "melon", "nexus", "onion", "pixel", "quill", "robin", "salsa", "tempo",
            "usher", "vocal", "wagon", "xerox", "yield", "zonal", "alien", "bliss",
            "chess", "dusty", "elbow", "fancy", "gecko", "happy", "irony", "jumbo",
            "kitty", "llama", "moose", "nerdy", "otter", "puppy", "query", "rhino",
            "snail", "tasty", "uncle", "vivid", "whale", "xylem", "yummy", "zippy",
            "alpha", "beta", "gamma", "theta", "omega", "sigma", "cloud", "storm",
            "windy", "sunny", "rainy", "snowy", "foggy", "crisp", "bland", "spicy",
            "sweet", "sour", "bitter", "salty", "fresh", "stale", "rough", "smooth",
            "hard", "soft", "heavy", "light", "solid", "fluid", "gas", "plasma",
            "metal", "wood", "glass", "stone", "brick", "paper", "cloth", "silk",
            "wool", "cotton", "nylon", "denim", "leather", "suede", "velvet", "lace",
            "red", "blue", "green", "yellow", "orange", "purple", "pink", "brown",
            "black", "white", "gray", "silver", "gold", "bronze", "copper", "iron",
            "steel", "brass", "zinc", "lead", "tin", "nickel", "chrome", "alloy",
            "circle", "square", "triangle", "oval", "cube", "sphere", "cone", "prism",
            "line", "curve", "angle", "point", "plane", "space", "time", "mass",
            "force", "power", "energy", "work", "heat", "light", "sound", "wave",
            "atom", "cell", "gene", "seed", "root", "leaf", "stem", "flower",
            "fruit", "nut", "shell", "bone", "skin", "hair", "eye", "ear",
            "nose", "mouth", "tooth", "tongue", "lip", "chin", "neck", "back",
            "arm", "hand", "leg", "foot", "toe", "finger", "thumb", "nail",
            "mind", "soul", "spirit", "dream", "thought", "idea", "plan", "goal",
            "hope", "fear", "joy", "sad", "love", "hate", "anger", "peace",
            "fast", "slow", "loud", "quiet", "near", "far", "high", "low",
            "big", "small", "wide", "deep", "long", "short", "thick", "thin"
        ).let { it.take(256) }

        init {
            require(WORDS.size == 256) { "Word list must have exactly 256 words" }
        }

        fun generateFingerprint(publicKey: ByteArray): String {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(publicKey)
            return (0..3).joinToString("-") { i ->
                val byteVal = hash[i].toInt() and 0xFF
                WORDS[byteVal]
            }
        }
    }
}
