
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

val JSONObject.asJsonSkeleton
    get() = JSONUtils.convertJsonToSkeleton(this)

val String.asJsonSkeleton
    get() = JSONUtils.convertJsonToSkeleton(this)

val String.asSkeleton
    get() = JSONUtils.convertJsonToSkeletonString(this)

inline val String.asJsonArrayOrNull: JSONArray?
    get()
    {
        var jsonArray: JSONArray? = null
        if (this.isEmpty())
        {
            return jsonArray
        }
        try
        {
            jsonArray = JSONArray(this)
        }
        catch (e: Exception)
        {
            //            KotlinUtils.printStackTrace(e)
        }
        return jsonArray
    }

inline val String.asJsonObjOrNull: JSONObject?
    get()
    {
        var jsonObj: JSONObject? = null
        if (this.isEmpty())
        {
            return jsonObj
        }
        try
        {
            jsonObj = JSONObject(this)
        }
        catch (e: Exception)
        {
            //            KotlinUtils.printStackTrace(e)
        }
        return jsonObj
    }

inline val String.asJsonObj: JSONObject
    get()
    {
        return JSONObject(this)
    }

inline val String.asJsonArray: JSONArray
    get()
    {
        return JSONArray(this)
    }

fun JSONArray.isEmpty(): Boolean {
    return this.length() == 0
}

fun JSONArray.isNotEmpty(): Boolean {
    return this.length() > 0
}

object JSONUtils
{
    private val importantKeys = setOf("code",
                                      "status",
                                      "error")

    fun convertJsonToSkeleton(jsonObject: JSONObject): JSONObject
    {
        val jsonSkeleton = JSONObject()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = anyToSkeleton(key, jsonObject.get(key))
            jsonSkeleton.put(key, value)
        }

        return jsonSkeleton
    }

    fun convertJsonToSkeleton(apiResponse: String?): JSONObject
    {
        return JSONObject().also {
            it.put("skeleton", convertJsonToSkeletonString(apiResponse))
        }
    }

    fun convertJsonToSkeletonString(apiResponse: String?): Any
    {
        if (apiResponse.isNullOrBlank())
        {
            return "Empty Response"
        }
        else
        {
            var any: Any?
            any = apiResponse.asJsonObjOrNull
            if (any == null) {
                any = apiResponse.asJsonArrayOrNull
            }
            if (any == null) {
                if (apiResponse == "true") {
                    any = true
                } else if (apiResponse == "false") {
                    any = false
                }
            }
            if (any == null) {
                any = apiResponse.toIntOrNull()
            }
            if (any == null) {
                any = apiResponse.toLongOrNull()
            }
            if (any == null) {
                any = apiResponse.toDoubleOrNull()
            }
            if (any == null) {
                any = apiResponse
            }
            return anyToSkeleton(null, any)
        }
    }

    private fun anyToSkeleton(key: String?, anything: Any?): Any
    {
        var any: Any? = anything

        if (anything is String) {
            any = anything.asJsonObjOrNull
            if (any == null) {
                any = anything.asJsonArrayOrNull
            }
            if (any == null) {
                any = anything
            }
        }

        if (any != null)
        {
            when (any)
            {
                is JSONObject ->
                {
                    return jsonObjectToSkeleton(any)
                }

                is JSONArray  ->
                {
                    return jsonArrayToSkeleton(any)
                }
            }

            if (importantKeys.contains(key))
            {
                return any
            }
            else
            {
                return when (any)
                {
                    is String ->
                    {
                        "String ${any.toString().length}"
                    }

                    is Int    ->
                    {
                        "Int ${any.toString().length}"
                    }

                    is Long   ->
                    {
                        "Long ${any.toString().length}"
                    }

                    is Double ->
                    {
                        "Double: ${any.toString().length}"
                    }

                    is Boolean    ->
                    {
                        return any
                    }

                    else      ->
                    {
                        any.toString()
                    }
                }
            }
        }
        else
        {
            return "null"
        }
    }

    fun String.redactDigits(): String {
        val value = this
        val digits = value.filter { it.isDigit() }
        if (digits.length > 3) {
            return value.filter { !it.isDigit() } + ", Digits ${digits.length}, hash: ${digits.hashCode()}"
        }
        return value
    }

    private fun jsonObjectToSkeleton(jsonObject: JSONObject): JSONObject
    {
        val skeletonJsonObject = JSONObject()

        jsonObject.keys().forEach { key ->
            skeletonJsonObject.put(key.redactDigits(), anyToSkeleton(key, jsonObject.get(key)))
        }

        return skeletonJsonObject
    }

    private fun jsonArrayToSkeleton(jsonArray: JSONArray): JSONArray
    {
        val skeletonJsonArray = JSONArray()

        for (i in 0 until jsonArray.length())
        {
            skeletonJsonArray.put(i, anyToSkeleton(null, jsonArray[i]))
        }
        return skeletonJsonArray
    }

    fun merge(vararg jsonObjects: JSONObject): JSONObject {
        val jsonObject = JSONObject()
        for (temp in jsonObjects) {
            val keys = temp.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                jsonObject.put(key, temp[key])
            }
        }
        return jsonObject
    }
}
