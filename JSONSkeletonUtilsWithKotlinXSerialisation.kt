import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull


val JsonObject.asJsonSkeleton
    get() = JSONUtils.convertJsonToSkeleton(this)

val String.asJsonSkeleton
    get() = JSONUtils.convertJsonToSkeleton(this)

val String.asSkeleton
    get() = JSONUtils.convertJsonToSkeletonString(this)

inline val String.asJsonArrayOrNull: JsonArray?
    get() {
        var jsonArray: JsonArray? = null
        if (this.isEmpty()) {
            return jsonArray
        }
        try {
            jsonArray = Json.parseToJsonElement(this).jsonArrayOrNull
        } catch (e: SerializationException) {
            //            Util.printStackTrace(e)
        }
        return jsonArray
    }

inline val String.asJsonObjOrNull: JsonObject?
    get() {
        var jsonObj: JsonObject? = null
        if (this.isEmpty()) {
            return jsonObj
        }
        try {
            jsonObj = Json.parseToJsonElement(this).jsonObjectOrNull
        } catch (e: SerializationException) {
            //            Util.printStackTrace(e)
        }
        return jsonObj
    }

inline val String.asJsonObj: JsonObject
    get() {
        return Json.parseToJsonElement(this).jsonObject
    }

inline val String.asJsonArray: JsonArray
    get() {
        return Json.parseToJsonElement(this).jsonArray
    }

object JSONUtils {
    private val importantKeys = setOf(
        "code",
        "status",
        //            "message",
        "error"
    )

    fun convertJsonToSkeleton(jsonObject: JsonObject): JsonObject {
        return buildJsonObject {
            jsonObject.forEach { entry ->
                val value = anyToSkeleton(entry.key, entry.value)
                put(entry.key, value)
            }
        }
    }

    fun convertJsonToSkeleton(apiResponse: String): JsonObject {
        return buildJsonObject {
            put("skeleton", convertJsonToSkeletonString(apiResponse))
        }
    }

    fun convertJsonToSkeletonString(apiResponse: String): JsonElement {
        if (apiResponse.isBlank()) {
            return JsonPrimitive("Empty Response")
        } else {
            var any: JsonElement?
            any = apiResponse.asJsonObjOrNull
            if (any == null) {
                any = apiResponse.asJsonArrayOrNull
            }
            if (any == null) {
                if (apiResponse == "true") {
                    any = JsonPrimitive(true)
                } else if (apiResponse == "false") {
                    any = JsonPrimitive(false)
                }
            }
            if (any == null) {
                apiResponse.toIntOrNull()?.let {
                    any = JsonPrimitive(it)
                }
            }
            if (any == null) {
                apiResponse.toLongOrNull()?.let {
                    any = JsonPrimitive(it)
                }
            }
            if (any == null) {
                apiResponse.toDoubleOrNull()?.let {
                    any = JsonPrimitive(it)
                }
            }
            if (any == null) {
                any = JsonPrimitive(apiResponse)
            }
            return anyToSkeleton("", any!!)
        }
    }

    private fun anyToSkeleton(key: String, anything: JsonElement): JsonElement {
        var any: JsonElement? = anything

        if (anything is JsonPrimitive && anything.isString) {
            any = anything.content.asJsonObjOrNull
            if (any == null) {
                any = anything.content.asJsonArrayOrNull
            }
            if (any == null) {
                any = anything
            }
        }

        if (any != null) {
            if (any is JsonObject) {
                return jsonObjectToSkeleton(any)
            }
            else if (any is JsonArray) {
                return jsonArrayToSkeleton(any)
            }

            if (importantKeys.contains(key)) {
                return any
            } else {
                return when {
                    any is JsonPrimitive && any.isString -> {
                        JsonPrimitive("String ${any.toString().length}")
                    }
                    any is JsonPrimitive && any.intOrNull != null -> {
                        JsonPrimitive("Int ${any.toString().length}")
                    }
                    any is JsonPrimitive && any.longOrNull != null -> {
                        JsonPrimitive("Long ${any.toString().length}")
                    }
                    any is JsonPrimitive && any.doubleOrNull != null -> {
                        JsonPrimitive("Double: ${any.toString().length}")
                    }
                    any is JsonPrimitive && any.booleanOrNull != null -> {
                        JsonPrimitive("Boolean: ${any.content}")
                    }
                    else -> {
                        JsonPrimitive(any.toString())
                    }
                }
            }
        } else {
            return JsonPrimitive("null")
        }
    }

    private fun String.redactDigits(): String {
        val value = this
        val digits = value.filter { it.isDigit() }
        if (digits.length > 3) {
            return value.filter { !it.isDigit() } + ", Digits ${digits.length}, hash: ${digits.hashCode()}"
        }
        return value
    }

    private fun jsonObjectToSkeleton(jsonObject: JsonObject): JsonObject {
        return buildJsonObject {
            jsonObject.forEach { entry ->
                val value = anyToSkeleton(entry.key, entry.value)
                put(entry.key.redactDigits(), value)
            }
        }
    }

    private fun jsonArrayToSkeleton(jsonArray: JsonArray): JsonArray {
        return buildJsonArray {
            jsonArray.forEach { jsonElement ->
                val value = anyToSkeleton("null", jsonElement)
                add(value)
            }
        }
    }

    fun merge(vararg jsonObjects: JsonObject): JsonObject {
        return buildJsonObject {
            for (temp in jsonObjects) {
                temp.forEach { entry ->
                    put(entry.key, entry.value)
                }
            }
        }
    }

    fun mergeAndSort(jsonArrays: List<JsonArray>): JsonArray {
        return buildJsonArray {
            val list = ArrayList<Pair<Int, JsonElement>>()

            for (temp in jsonArrays) {
                val size = temp.size
                var i = 0
                while (i < size) {
                    list.add(Pair(i, temp[i]))
                    i++
                }
            }

            list.sortWith { o1, o2 ->
                o1.component1()
                    .compareTo(o2.component1())
            }

            list.forEach {
                add(it.second)
            }
        }
    }
}
