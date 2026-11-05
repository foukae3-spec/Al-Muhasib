package com.example.util.prayer

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ponytail: robust multi-engine geocoder with automatic CalculationMethod determination
object OnlineCitySearcher {

    fun resolveCalculationMethod(latitude: Double, longitude: Double, countryHint: String? = null): CalculationMethod {
        val country = countryHint?.lowercase() ?: ""
        if (country.contains("algeria") || country.contains("الجزائر") || country.contains("dz")) {
            return CalculationMethod.ALGERIA
        }
        if (country.contains("egypt") || country.contains("مصر") || country.contains("eg")) {
            return CalculationMethod.EGYPTIAN
        }
        if (country.contains("saudi") || country.contains("سعودي") || country.contains("مكة") || country.contains("ksa")) {
            return CalculationMethod.UMM_AL_QURA
        }
        if (country.contains("emirates") || country.contains("إمارات") || country.contains("qatar") || country.contains("قطر") ||
            country.contains("kuwait") || country.contains("كويت") || country.contains("oman") || country.contains("عمان") ||
            country.contains("bahrain") || country.contains("بحرين")) {
            return CalculationMethod.GULF
        }
        if (country.contains("turkey") || country.contains("تركيا") || country.contains("türkiye")) {
            return CalculationMethod.DIYANET
        }
        if (country.contains("pakistan") || country.contains("باكستان") || country.contains("india") || country.contains("الهند") || country.contains("bangladesh")) {
            return CalculationMethod.KARACHI
        }
        if (country.contains("morocco") || country.contains("المغرب") || country.contains("tunisia") || country.contains("تونس") || country.contains("libya") || country.contains("ليبيا")) {
            return CalculationMethod.ALGERIA
        }
        if (country.contains("united states") || country.contains("أمريكا") || country.contains("usa") || country.contains("canada") || country.contains("كندا")) {
            return CalculationMethod.ISNA
        }

        // Geolocation bounding box fallback
        // Algeria / Maghreb bounding box: Lat 18..38, Lon -13..12
        if (latitude in 18.0..38.0 && longitude in -13.0..12.0) {
            return CalculationMethod.ALGERIA
        }
        // Egypt bounding box: Lat 21..32, Lon 24..37
        if (latitude in 21.0..32.0 && longitude in 24.0..37.0) {
            return CalculationMethod.EGYPTIAN
        }
        // Arabian Peninsula
        if (latitude in 12.0..33.0 && longitude in 34.0..60.0) {
            return CalculationMethod.UMM_AL_QURA
        }
        // Turkey
        if (latitude in 35.0..43.0 && longitude in 25.0..45.0) {
            return CalculationMethod.DIYANET
        }

        return CalculationMethod.MUSLIM_WORLD_LEAGUE
    }

    private fun cleanQuery(query: String): String {
        return query.trim()
            .replace("^ولاية\\s+".toRegex(), "")
            .replace("^مدينة\\s+".toRegex(), "")
            .replace("^بلدية\\s+".toRegex(), "")
            .replace("^دائرة\\s+".toRegex(), "")
            .replace("^wilaya\\s+(de\\s+)?".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("^ville\\s+(de\\s+)?".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
    }

    fun searchOnline(query: String): List<CityLocation> {
        val clean = cleanQuery(query)
        if (clean.length < 2) return emptyList()

        val results = searchNominatim(clean)
        if (results.isNotEmpty()) return results

        // Fallback to Photon Komoot engine if Nominatim returned 0 results
        return searchPhoton(clean)
    }

    private fun searchNominatim(cleanQuery: String): List<CityLocation> {
        return try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=10&accept-language=ar,en")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "AlMuhasibApp/1.0 (contact: info@almuhasib.app)")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"

            if (conn.responseCode != 200) {
                conn.disconnect()
                return emptyList()
            }

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val jsonArray = JSONArray(body)
            val list = mutableListOf<CityLocation>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val lat = item.getString("lat").toDoubleOrNull() ?: continue
                val lon = item.getString("lon").toDoubleOrNull() ?: continue
                val displayName = item.optString("display_name", cleanQuery)
                val name = item.optString("name", cleanQuery)

                val parts = displayName.split(",").map { it.trim() }
                val cityName = if (parts.isNotEmpty()) parts.first() else name
                val countryName = if (parts.size > 1) parts.last() else "عالمي"

                val id = "GEO_${(lat * 1000).toInt()}_${(lon * 1000).toInt()}"
                val calcMethod = resolveCalculationMethod(lat, lon, countryName)

                list.add(
                    CityLocation(
                        id = id,
                        arabicName = cityName,
                        englishName = name,
                        countryArabic = countryName,
                        countryEnglish = countryName,
                        latitude = lat,
                        longitude = lon,
                        timezoneId = java.time.ZoneId.systemDefault().id,
                        defaultMethod = calcMethod
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchPhoton(cleanQuery: String): List<CityLocation> {
        return try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = URL("https://photon.komoot.io/api/?q=$encoded&limit=10")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"

            if (conn.responseCode != 200) {
                conn.disconnect()
                return emptyList()
            }

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val root = JSONObject(body)
            val features = root.optJSONArray("features") ?: return emptyList()
            val list = mutableListOf<CityLocation>()

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.optJSONObject("geometry") ?: continue
                val coords = geometry.optJSONArray("coordinates") ?: continue
                val lon = coords.optDouble(0)
                val lat = coords.optDouble(1)

                val props = feature.optJSONObject("properties") ?: JSONObject()
                val name = props.optString("name", cleanQuery)
                val country = props.optString("country", "عالمي")
                val city = props.optString("city", props.optString("state", name))

                val id = "PHO_${(lat * 1000).toInt()}_${(lon * 1000).toInt()}"
                val calcMethod = resolveCalculationMethod(lat, lon, country)

                list.add(
                    CityLocation(
                        id = id,
                        arabicName = city,
                        englishName = name,
                        countryArabic = country,
                        countryEnglish = country,
                        latitude = lat,
                        longitude = lon,
                        timezoneId = java.time.ZoneId.systemDefault().id,
                        defaultMethod = calcMethod
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun reverseGeocodeOnline(latitude: Double, longitude: Double): String? {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&accept-language=ar")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "AlMuhasibApp/1.0 (contact: info@almuhasib.app)")
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode != 200) {
                conn.disconnect()
                return null
            }

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val root = JSONObject(body)
            val address = root.optJSONObject("address")
            if (address != null) {
                val city = address.optString("city", address.optString("town", address.optString("village", address.optString("state", ""))))
                val country = address.optString("country", "")
                if (city.isNotBlank() && country.isNotBlank()) "$city، $country"
                else if (city.isNotBlank()) city
                else country.ifBlank { null }
            } else {
                root.optString("name").ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }
}
