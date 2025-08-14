package techcourse.herobeans.enums

enum class Grams(val value: Int) {
    G250(250),
    G500(500),
    G1000(1000),
    ;

    companion object {
        fun of(value: Int): Grams {
            require(value in MIN_GRAMS..MAX_GRAMS) { "Grams must be between $MIN_GRAMS and $MAX_GRAMS" }
            return Grams.entries.find { it.value == value }
                ?: throw IllegalArgumentException("No matching Grams for $value")
        }

        private const val MIN_GRAMS = 250
        private const val MAX_GRAMS = 1000
    }
}
