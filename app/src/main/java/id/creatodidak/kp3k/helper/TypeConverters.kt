package id.creatodidak.kp3k.helper

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromString(value: String?): TypeLahan? = value?.let { TypeLahan.valueOf(it) }

    @TypeConverter
    fun typeToString(type: TypeLahan?): String? = type?.name

    @TypeConverter
    fun fromMediaString(value: String?): MediaType? = value?.let { MediaType.valueOf(it) }

    @TypeConverter
    fun mediaToString(type: MediaType?): String? = type?.name

    @TypeConverter
    fun fromTypeOwnerString(value: String?): TypeOwner? = value?.let { TypeOwner.valueOf(it) }

    @TypeConverter
    fun typeOwnerToString(type: TypeOwner?): String? = type?.name

    @TypeConverter
    fun fromIsGapkiString(value: String?): IsGapki? = value?.let { IsGapki.valueOf(it) }

    @TypeConverter
    fun isGapkiToString(value: IsGapki?): String? = value?.name

    @TypeConverter
    fun fromSumberString(value: String?): SumberBibit? = value?.let { SumberBibit.valueOf(it) }

    @TypeConverter
    fun sumberToString(value: SumberBibit?): String? = value?.name
}

enum class TypeLahan { MONOKULTUR, TUMPANGSARI, PBPH, PERHUTANANSOSIAL }
enum class MediaType { IMAGE, VIDEO, AUDIO }
enum class TypeOwner { PRIBADI, PEMDES, PEMDA, PEMPROV, PERUSAHAAN, POKTAN, KWT }
enum class IsGapki { YA, TIDAK }
enum class SumberBibit { MANDIRI, POLRI, PEMERINTAH }
