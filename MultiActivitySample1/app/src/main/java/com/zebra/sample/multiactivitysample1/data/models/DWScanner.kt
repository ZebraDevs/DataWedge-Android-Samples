package com.zebra.sample.multiactivitysample1.data.models

data class DWScanner(
    val name:String,
    val connectionState:Boolean,
    val index:Int,
    val identifier:String
){
    override fun toString(): String {
        return name
    }
}