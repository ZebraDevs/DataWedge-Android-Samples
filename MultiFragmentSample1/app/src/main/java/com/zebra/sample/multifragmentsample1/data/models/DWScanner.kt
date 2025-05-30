package com.zebra.sample.multifragmentsample1.data.models

data class DWScanner(
    val name:String,
    val connectionState:Boolean,
    val index:String,
    val identifier:String
){
    override fun toString(): String {
        return name
    }
}