package id.creatodidak.kp3k.api.newModel

import java.util.Date

data class Kuartal (
    val id : Int,
    val tanggalStart: Date,
    val tanggalEnd: Date,
    val tahun : Int,
    val name : String,
){
    override fun toString(): String = name
}