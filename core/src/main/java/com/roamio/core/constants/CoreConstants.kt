package com.roamio.core.constants

/**
 * All constants for the core module organized by domain.
 *
 * @author udit
 */
object CoreConstants {

    object Network {
        const val REQUEST_TIMEOUT_MILLIS = 60_000L
        const val CONNECT_TIMEOUT_MILLIS = 10_000L
        const val HTTP_STATUS_CODE_THRESHOLD = 300
        const val TIMESTAMP_FORMAT = "HH:mm:ss.SSS"
        const val HTTP_STATUS_BUSY = 503
        const val HTTP_STATUS_TOO_MANY_REQUESTS = 429
        const val HTTP_STATUS_GATEWAY_TIMEOUT = 504
        const val USER_AGENT = "Roamio/1.0 (Android; travel companion; https://github.com/roamio)"
        const val HEADER_USER_AGENT = "User-Agent"
    }

    object Api {
        const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com"
        const val OPEN_METEO_FORECAST_PATH = "/v1/forecast"
        const val OVERPASS_BASE_URL = "https://overpass-api.de"
        const val OVERPASS_FALLBACK_BASE_URL = "https://overpass.private.coffee"
        const val OVERPASS_FALLBACK_BASE_URL_ALT = "https://overpass.osm.jp"
        const val OVERPASS_INTERPRETER_PATH = "/api/interpreter"
        const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org"
        const val WIKIPEDIA_API_PATH = "/w/api.php"
        const val FRANKFURTER_BASE_URL = "https://api.frankfurter.app"
        const val FRANKFURTER_LATEST_PATH = "/latest"
        const val FRANKFURTER_CURRENCIES_PATH = "/currencies"
        const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org"
        const val NOMINATIM_REVERSE_PATH = "/reverse"
        const val PARAM_LAT = "lat"
        const val PARAM_LON = "lon"

        const val PARAM_LATITUDE = "latitude"
        const val PARAM_LONGITUDE = "longitude"
        const val PARAM_CURRENT = "current"
        const val PARAM_DAILY = "daily"
        const val PARAM_TIMEZONE = "timezone"
        const val PARAM_FORECAST_DAYS = "forecast_days"
        const val PARAM_DATA = "data"
        const val PARAM_ACTION = "action"
        const val PARAM_FORMAT = "format"
        const val PARAM_PROP = "prop"
        const val PARAM_EXPLAINTEXT = "explaintext"
        const val PARAM_TITLES = "titles"
        const val PARAM_EXCHARS = "exchars"
        const val PARAM_AMOUNT = "amount"
        const val PARAM_FROM = "from"
        const val PARAM_TO = "to"

        const val CURRENT_WEATHER_FIELDS = "temperature_2m,weather_code,wind_speed_10m"
        const val DAILY_WEATHER_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min"
        const val TIMEZONE_AUTO = "auto"
        const val FORECAST_DAYS = "5"
        const val WIKI_ACTION_QUERY = "query"
        const val WIKI_FORMAT_JSON = "json"
        const val WIKI_PROP_EXTRACTS = "extracts"
        const val WIKI_EXPLAINTEXT = "1"
        const val WIKI_EXCHARS = "200"
        const val OVERPASS_RADIUS_METERS = 3000
        const val OVERPASS_RESULT_LIMIT = 20
        const val OVERPASS_FETCH_LIMIT = 50
        const val OVERPASS_REQUEST_TIMEOUT_MILLIS = 25_000L
    }

    object Location {
        const val FALLBACK_LATITUDE = 51.5074
        const val FALLBACK_LONGITUDE = -0.1278
        const val FALLBACK_CITY_NAME = "London"
        const val CURRENT_LOCATION_LABEL = "Your location"
        const val PLACE_NAME_FORMAT = "%1\$s, %2\$s"
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }

    object Preferences {
        const val DATASTORE_NAME = "roamio_preferences"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    object Logging {
        const val API_LOGGER_TAG = "API_CALLS"
        const val REQUEST_PREFIX = "REQUEST"
        const val RESPONSE_PREFIX = "RESPONSE"
        const val TIMING_PREFIX = "TIMING"
        const val LOG_SEPARATOR = "═══════════════════════════════════════════════════════════════"
        const val SINGLE_SPACE = " "
        const val DOUBLE_SPACE = "  "
        const val STATUS_FORMAT = "Status: %s %d - %s"
        const val REQUEST_ID_FORMAT = "req_%d_%d"
        const val REQUEST_KEYWORD = "REQUEST:"
        const val RESPONSE_KEYWORD = "RESPONSE:"
        const val HEADERS_KEYWORD = "HEADERS:"
        const val BODY_KEYWORD = "BODY:"
        const val HEADERS_ICON = "H"
        const val BODY_ICON = "B"
        const val INFO_ICON = "I"
        const val STATUS_OK_EMOJI = "OK"
        const val STATUS_REDIRECT_EMOJI = "REDIRECT"
        const val STATUS_CLIENT_ERROR_EMOJI = "CLIENT_ERROR"
        const val STATUS_SERVER_ERROR_EMOJI = "SERVER_ERROR"
        const val STATUS_UNKNOWN_EMOJI = "UNKNOWN"
        const val STATUS_RANGE_OK_START = 200
        const val STATUS_RANGE_OK_END = 299
        const val STATUS_RANGE_REDIRECT_START = 300
        const val STATUS_RANGE_REDIRECT_END = 399
        const val STATUS_RANGE_CLIENT_ERROR_START = 400
        const val STATUS_RANGE_CLIENT_ERROR_END = 499
        const val STATUS_RANGE_SERVER_ERROR_START = 500
        const val STATUS_RANGE_SERVER_ERROR_END = 599
        const val RANDOM_ID_BOUND = 1000
        const val MSG_LOCATION_UNAVAILABLE = "Location unavailable"
    }

    object Errors {
        const val NETWORK = "Network error. Check your connection."
        const val UNKNOWN = "Something went wrong."
        const val EMPTY_RESULTS = "No results found nearby."
        const val SERVICE_BUSY = "That service is busy right now. Try again in a moment."
        const val HTTP_UNAVAILABLE = "503"
        const val HTTP_TOO_MANY_REQUESTS = "429"
        const val HTTP_GATEWAY_TIMEOUT = "504"
    }

    object Overpass {
        const val QUERY_ATTRACTIONS =
            "[out:json][timeout:25];nwr[\"tourism\"~\"attraction|museum|viewpoint|artwork|gallery\"](around:%d,%f,%f);out center %d;"
        const val QUERY_RESTAURANTS =
            "[out:json][timeout:25];nwr[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out center %d;"
        const val TAG_NAME = "name"
        const val TAG_TOURISM = "tourism"
        const val TAG_CUISINE = "cuisine"
        const val UNKNOWN_NAME = "Unknown"
    }
}
