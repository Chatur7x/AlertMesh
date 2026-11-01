package com.alertmesh.protocol

interface CounterStore {
    fun read(): Long
    fun write(counter: Long): Boolean
}
