package id.creatodidak.kp3k.api.newModel

data class Contact (
    val id : Int,
    val nrp : String,
    val nama : String,
    val jabatan : String,
){
    override fun toString(): String = nama
}