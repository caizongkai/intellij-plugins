package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages.getLanguageForShortCode
import org.languagetool.language.*

enum class Lang(val shortCode: String,
                private val lang: Language = getLanguageForShortCode(shortCode, emptyList())!!,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet()) {
    BRITISH_ENGLISH("en", BritishEnglish(),
            setOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE"),
            setOf("WORD_CONTAINS_UNDERSCORE", "EN_QUOTES")),
    AMERICAN_ENGLISH("en", AmericanEnglish(),
            setOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE"),
            setOf("WORD_CONTAINS_UNDERSCORE", "EN_QUOTES")),
    RUSSIAN("ru", Russian(),
            setOf("ABREV_DOT2", "KAK_VVODNOE", "PARTICLE_JE", "po_povodu_togo", "tak_skazat", "kak_bi", "O_tom_chto", "kosvennaja_rech")),
    PERSIAN("fa"),
    FRENCH("fr"),
    GERMAN("de"),
    POLISH("pl"),
    CATALAN("ca"),
    ITALIAN("it"),
    BRETON("br"),
    DUTCH("nl"),
    PORTUGUES("pt"),
    BELARUSIAN("be"),
    CHINESE("zh"),
    DANISH("da"),
    GALICIAN("gl"),
    GREEK("el"),
    JAPANESE("ja"),
    KHMER("km"),
    ROMANIAN("ro"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("sv"),
    TAMIL("ta"),
    TAGALOG("tl"),
    UKRANIAN("uk");

    companion object {
        operator fun get(lang: Language): Lang? = values().find { it.shortCode == lang.shortCode }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = Lang.values().sortedBy { it.name }
    }

    val displayName: String = name.toLowerCase().capitalize()

    fun toLanguage() = lang

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        val toEnable = tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }
        toEnable.forEach {
            tool.enableRule(it.id)
        }

        val toDisable = tool.allRules.filter { rule -> disabledRules.any { rule.id.contains(it) } }
        toDisable.forEach {
            tool.disableRule(it.id)
        }
    }
}