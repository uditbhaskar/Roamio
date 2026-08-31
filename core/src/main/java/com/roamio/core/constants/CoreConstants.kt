package com.roamio.core.constants

/**
 * All constants for the core module organized by domain.
 *
 * @author udit
 */
object CoreConstants {

    /**
     * HTTP client timeouts, User-Agent, and status-code bands.
     *
     * @author udit
     */
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

    /**
     * Public API hosts, paths, and query parameter names.
     *
     * @author udit
     */
    object Api {
        const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com"
        const val OPEN_METEO_FORECAST_PATH = "/v1/forecast"
        const val OVERPASS_BASE_URL = "https://overpass-api.de"
        const val OVERPASS_FALLBACK_BASE_URL = "https://overpass.private.coffee"
        const val OVERPASS_FALLBACK_BASE_URL_KUMI = "https://overpass.kumi.systems"
        const val OVERPASS_INTERPRETER_PATH = "/api/interpreter"
        const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org"
        const val WIKIVOYAGE_BASE_URL = "https://en.wikivoyage.org"
        const val WIKIPEDIA_API_PATH = "/w/api.php"
        const val WIKIPEDIA_REST_SUMMARY_PATH = "/api/rest_v1/page/summary/"
        const val WIKI_PROP_EXTRACTS = "extracts"
        const val WIKI_EXSECTIONFORMAT_PLAIN = "plain"
        const val WIKI_SEARCH_CANDIDATES = "3"
        const val FRANKFURTER_BASE_URL = "https://api.frankfurter.app"
        const val FRANKFURTER_LATEST_PATH = "/latest"
        const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org"
        const val NOMINATIM_REVERSE_PATH = "/reverse"
        const val NOMINATIM_SEARCH_PATH = "/search"
        const val PEXELS_BASE_URL = "https://api.pexels.com"
        const val PEXELS_SEARCH_PATH = "/v1/search"
        const val OPEN_METEO_ELEVATION_PATH = "/v1/elevation"
        const val WIKIMEDIA_FILE_PATH = "https://commons.wikimedia.org/wiki/Special:FilePath/"
        const val COMMONS_BASE_URL = "https://commons.wikimedia.org"
        const val OPENVERSE_BASE_URL = "https://api.openverse.org"
        const val OPENVERSE_IMAGES_PATH = "/v1/images/"
        const val PARAM_LAT = "lat"
        const val PARAM_LON = "lon"
        const val PARAM_Q = "q"
        const val PARAM_LIMIT = "limit"
        const val PARAM_ADDRESSDETAILS = "addressdetails"
        const val PARAM_VIEWBOX = "viewbox"
        const val PARAM_BOUNDED = "bounded"
        const val NOMINATIM_BOUNDED = "1"
        const val NOMINATIM_FOOD_QUERY = "cafe"
        const val NOMINATIM_FOOD_VIEWBOX_DEG = 0.05
        const val UPLOAD_WIKIMEDIA_PREFIX = "https://upload.wikimedia.org/"
        const val PARAM_QUERY = "query"
        const val PARAM_PER_PAGE = "per_page"
        const val PARAM_PITHUMBSIZE = "pithumbsize"
        const val PARAM_PIPROP = "piprop"
        const val PARAM_REDIRECTS = "redirects"
        const val PARAM_GENERATOR = "generator"
        const val PARAM_GSRSEARCH = "gsrsearch"
        const val PARAM_GSRLIMIT = "gsrlimit"
        const val PARAM_GSRNAMESPACE = "gsrnamespace"
        const val PARAM_IIPROP = "iiprop"
        const val PARAM_IIURLWIDTH = "iiurlwidth"
        const val PARAM_GGSCOORD = "ggscoord"
        const val PARAM_GGSRADIUS = "ggsradius"
        const val PARAM_GGSLIMIT = "ggslimit"
        const val PARAM_PAGE_SIZE = "page_size"
        const val WIKI_GENERATOR_GEOSEARCH = "geosearch"
        const val WIKI_GEO_RADIUS_METERS = "10000"
        const val WIKI_GEO_LIMIT = "20"
        const val WIKI_PROP_EXTRACTS_IMAGES_COORDS = "extracts|pageimages|coordinates"
        const val WIKI_COORD_SEPARATOR = "|"
        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_REFERER = "Referer"
        const val WIKIPEDIA_REFERER = "https://en.wikipedia.org/"
        const val WIKIMEDIA_HOST = "wikimedia.org"
        const val WIKIPEDIA_HOST = "wikipedia.org"
        const val NOMINATIM_SEARCH_LIMIT = "6"
        const val ADDRESS_DETAILS_ENABLED = "1"
        const val PEXELS_PER_PAGE = "1"
        const val WIKI_THUMB_SIZE = "800"
        const val PHOTO_DISPLAY_PX = 800
        const val WIKI_THUMB_PX_REGEX = "/(\\d+)px-"
        const val IMAGE_DISK_CACHE_DIR = "image_cache"
        const val IMAGE_DISK_CACHE_BYTES = 80L * 1024L * 1024L
        const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_CROSSFADE_MILLIS = 120
        const val WIKI_PROP_EXTRACTS_IMAGES = "extracts|pageimages"
        const val WIKI_PIPROP_THUMB = "thumbnail"
        const val WIKI_REDIRECTS = "1"
        const val WIKI_GENERATOR_SEARCH = "search"
        const val WIKI_SEARCH_LIMIT = "1"
        const val WIKI_PROP_PAGEIMAGES = "pageimages"
        const val WIKI_PROP_IMAGEINFO = "imageinfo"
        const val COMMONS_FILE_NAMESPACE = "6"
        const val COMMONS_IIPROP = "url"
        const val OPENVERSE_PAGE_SIZE = "1"
        const val PHOTO_TOPIC_HIKING = "Hiking"
        const val PHOTO_TOPIC_KAYAKING = "Kayaking"
        const val PHOTO_TOPIC_BIKING = "Cycling"
        const val PHOTO_TOPIC_CAFE = "Cafe"
        const val FALLBACK_HIKING_FILE = "Walker_above_the_Sea_of_Fog.jpg"
        const val FALLBACK_KAYAKING_FILE = "Kayak.jpg"
        const val FALLBACK_BIKING_FILE = "Bicycle.jpg"
        const val FALLBACK_CAFE_FILE = "Coffee.jpg"
        const val FALLBACK_CITY_FILE = "Prague.jpg"
        const val SEED_FILE_LONDON = "Tower_Bridge_London.jpg"
        const val SEED_FILE_BERGEN = "Bryggen.jpg"
        const val SEED_FILE_INNSBRUCK = "Innsbruck.jpg"
        const val SEED_FILE_INTERLAKEN = "Interlaken.jpg"
        const val SEED_FILE_BANFF = "Banff.jpg"
        const val SEED_FILE_QUEENSTOWN = "Queenstown.jpg"
        const val SEED_FILE_REYKJAVIK = "Reykjavik.jpg"
        const val SEED_FILE_CHAMONIX = "Chamonix.jpg"
        const val SEED_FILE_BARCELONA = "Barcelona_skyline.jpg"
        const val SEED_FILE_BERGEN_WATER = "Sognefjord.jpg"
        const val SEED_FILE_INNSBRUCK_WATER = "Inn_River.jpg"
        const val SEED_FILE_INTERLAKEN_WATER = "Lake_Thun.jpg"
        const val SEED_FILE_BANFF_WATER = "Lake_Louise.jpg"
        const val SEED_FILE_QUEENSTOWN_WATER = "Lake_Wakatipu.jpg"
        const val SEED_FILE_LONDON_WATER = "River_Thames.jpg"
        const val SEED_FILE_REYKJAVIK_WATER = "Jokulsarlon.jpg"
        const val SEED_FILE_CHAMONIX_WATER = "Mer_de_Glace.jpg"
        const val SEED_FILE_BARCELONA_WATER = "Barcelona_Beach.jpg"
        const val ACTIVITY_KAYAK = "kayak"
        const val ACTIVITY_WATER = "water"
        const val ACTIVITY_BIKE = "bike"
        const val ACTIVITY_CYCL = "cycl"
        const val ACTIVITY_CAFE = "cafe"
        const val ACTIVITY_FOOD = "restaurant"
        const val ACTIVITY_PLACE = "place"

        const val PARAM_LATITUDE = "latitude"
        const val PARAM_LONGITUDE = "longitude"
        const val PARAM_CURRENT = "current"
        const val PARAM_TIMEZONE = "timezone"
        const val PARAM_DATA = "data"
        const val PARAM_ACTION = "action"
        const val PARAM_FORMAT = "format"
        const val PARAM_PROP = "prop"
        const val PARAM_EXPLAINTEXT = "explaintext"
        const val PARAM_TITLES = "titles"
        const val PARAM_EXCHARS = "exchars"
        const val PARAM_EXSECTIONFORMAT = "exsectionformat"
        const val PARAM_AMOUNT = "amount"
        const val PARAM_FROM = "from"
        const val PARAM_TO = "to"

        const val CURRENT_WEATHER_FIELDS = "temperature_2m,weather_code"
        const val TIMEZONE_AUTO = "auto"
        const val WIKI_ACTION_QUERY = "query"
        const val WIKI_FORMAT_JSON = "json"
        const val WIKI_EXPLAINTEXT = "1"
        const val WIKI_EXCHARS = "1200"
        const val OVERPASS_RESULT_LIMIT = 20
        const val OVERPASS_REQUEST_TIMEOUT_MILLIS = 12_000L
        const val HOME_NEARBY_TIMEOUT_MILLIS = 8_000L
        const val PLACE_FOOD_TIMEOUT_MILLIS = 2_500L
    }

    /**
     * Fallback coordinates and place-name formatting.
     *
     * @author udit
     */
    object Location {
        const val FALLBACK_LATITUDE = 51.5074
        const val FALLBACK_LONGITUDE = -0.1278
        const val FALLBACK_CITY_NAME = "London"
        const val FALLBACK_COUNTRY_CODE = "GB"
        const val CURRENT_LOCATION_LABEL = "Your location"
        const val PLACE_NAME_FORMAT = "%1\$s, %2\$s"
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }

    /**
     * DataStore file name and preference keys.
     *
     * @author udit
     */
    object Preferences {
        const val DATASTORE_NAME = "roamio_preferences"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_SAVED_PLACES = "saved_places"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_USE_CELSIUS = "use_celsius"
        const val KEY_HOME_CURRENCY = "home_currency"
        const val DEFAULT_HOME_CURRENCY = "USD"
    }

    /**
     * Structured API log prefixes and status labels.
     *
     * @author udit
     */
    object Logging {
        const val API_LOGGER_TAG = "API_CALLS"
        const val REQUEST_PREFIX = "REQUEST"
        const val RESPONSE_PREFIX = "RESPONSE"
        const val TIMING_PREFIX = "TIMING"
        const val LOG_SEPARATOR = "═══════════════════════════════════════════════════════════════"
        const val CACHE_KEY_SEPARATOR = "|"
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

    /**
     * User-facing error copy shared by repositories.
     *
     * @author udit
     */
    object Errors {
        const val NETWORK = "Network error. Check your connection."
        const val UNKNOWN = "Something went wrong."
        const val EMPTY_RESULTS = "No results found nearby."
        const val SERVICE_BUSY = "That service is busy right now. Try again in a moment."
        const val HTTP_UNAVAILABLE = "503"
        const val HTTP_TOO_MANY_REQUESTS = "429"
        const val HTTP_GATEWAY_TIMEOUT = "504"
    }

    /**
     * Generic copy used when Wikipedia has no extract yet.
     *
     * @author udit
     */
    object Copy {
        const val CITY = "A walkable base for trails, water, and a good meal after the day."
        const val HIKING = "Ridges, forest paths, and viewpoints within easy reach of town."
        const val KAYAKING = "Calm water, shoreline launches, and a day on the lake or fjord."
        const val BIKING = "Riverside paths and quiet roads made for an easy ride."
        const val POPULAR = "A well-known stop on the Roamio outdoor catalog."
        const val CAFE = "Somewhere close for coffee and a bite after the day."
    }

    /**
     * Filters that drop website and generic Wikipedia pages from place search.
     *
     * @author udit
     */
    object Wiki {
        const val DISAMBIGUATION = "disambiguation"
        const val LIST_PREFIX = "list of"
        const val DOMAIN_DOT = '.'
        const val MIN_TOKEN_LENGTH = 4
        val GENERIC_TITLES = setOf(
            "hiking",
            "kayaking",
            "cycling",
            "canoeing",
            "mountain biking",
        )
        val HIKING_HINTS = listOf(
            "hike",
            "hiking",
            "trail",
            "peak",
            "mountain",
            "ridge",
            "summit",
            "viewpoint",
            "nature reserve",
            "forest",
            "hill",
            "pass",
            "alp",
            "fell",
        )
        val KAYAKING_HINTS = listOf(
            "kayak",
            "canoe",
            "lake",
            "river",
            "fjord",
            "bay",
            "inlet",
            "reservoir",
            "canal",
            "harbour",
            "harbor",
            "waterfront",
        )
        val BIKING_HINTS = listOf(
            "cycle",
            "cycling",
            "bicycle",
            "bike",
            "greenway",
            "rail trail",
            "mountain bike",
            "mtb",
        )
        val SKIP_NAME_HINTS = listOf(
            "rental",
            "shop",
            "store",
            "parking",
            "toilet",
            "kiosk",
            "automat",
        )
    }

    /**
     * Overpass QL templates and OSM tag keys.
     *
     * @author udit
     */
    object Overpass {
        const val QUERY_RESTAURANTS =
            "[out:json][timeout:12];(" +
                "nwr[\"amenity\"=\"cafe\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"amenity\"=\"restaurant\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_HIKING =
            "[out:json][timeout:15];(" +
                "nwr[\"route\"=\"hiking\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "node[\"natural\"=\"peak\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"tourism\"=\"viewpoint\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"leisure\"=\"nature_reserve\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_HIKING_FULL =
            "[out:json][timeout:15];(" +
                "nwr[\"natural\"=\"wood\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"leisure\"=\"park\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "way[\"highway\"=\"path\"][\"name\"][\"sac_scale\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_KAYAKING =
            "[out:json][timeout:15];(" +
                "nwr[\"sport\"=\"canoe\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"sport\"=\"kayak\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"natural\"=\"water\"][\"water\"=\"lake\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"natural\"=\"water\"][\"water\"=\"reservoir\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"waterway\"=\"river\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"natural\"=\"bay\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_KAYAKING_FULL =
            "[out:json][timeout:15];(" +
                "nwr[\"natural\"=\"water\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"leisure\"=\"marina\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"amenity\"=\"boat_rental\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_BIKING =
            "[out:json][timeout:15];(" +
                "nwr[\"route\"=\"bicycle\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"route\"=\"mtb\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "way[\"highway\"=\"cycleway\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val QUERY_BIKING_FULL =
            "[out:json][timeout:15];(" +
                "nwr[\"leisure\"=\"park\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                "nwr[\"amenity\"=\"bicycle_rental\"][\"name\"](around:%1\$d,%2\$f,%3\$f);" +
                ");out center %4\$d;"
        const val TAG_NAME = "name"
        const val TAG_CUISINE = "cuisine"
        const val TAG_ELE = "ele"
        const val TAG_IMAGE = "image"
        const val TAG_WIKIMEDIA = "wikimedia_commons"
        const val TAG_OPENING_HOURS = "opening_hours"
        const val TAG_ADDR_STREET = "addr:street"
        const val TAG_ADDR_HOUSENUMBER = "addr:housenumber"
        const val TAG_ADDR_CITY = "addr:city"
        const val ACTIVITY_RADIUS_METERS = 20000
        const val FOOD_RADIUS_METERS = 5000
        const val FOOD_RESULT_LIMIT = 5
        const val WIKIMEDIA_FILE_PREFIX = "File:"
        const val TYPE_NODE = "node"
        const val FALLBACK_ELEMENT_ID = 0L
    }

    /**
     * ISO codes offered on Convert and Settings.
     *
     * @author udit
     */
    object Currency {
        val CODES = listOf("USD", "EUR", "GBP", "NOK", "SEK", "INR", "JPY")
    }

    /**
     * Haversine, walk-time, and flag-emoji helpers.
     *
     * @author udit
     */
    object Geo {
        const val EARTH_RADIUS_KM = 6371.0
        const val WALK_MINUTES_PER_KM = 12.0
        const val KM_FORMAT = "%.1f"
        const val ELEVATION_FORMAT = "%,d"
        const val FLAG_REGIONAL_INDICATOR_A = 0x1F1E6
        const val LATIN_A = 0x41
        const val COUNTRY_CODE_LENGTH = 2
        const val SAVE_COORD_EPSILON = 0.0005
        const val GEO_ID_SCALE = 10_000.0
        const val POPULAR_MAX_KM = 800.0
    }
}
