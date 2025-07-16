package id.creatodidak.kp3k.api.newModel

data class MasaTanam (
    val id : Int,
    val masatanam : String,
    val name : String,
){
    override fun toString(): String = name
}