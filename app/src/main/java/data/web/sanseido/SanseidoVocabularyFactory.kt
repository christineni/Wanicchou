package data.web.sanseido

import data.room.entity.Vocabulary
import data.arch.vocab.IVocabularyFactory
import data.arch.lang.EnglishVocabulary
import data.arch.lang.JapaneseVocabulary
import java.util.regex.Pattern

object SanseidoVocabularyFactory : IVocabularyFactory {
    private const val EXACT_WORD_REGEX = "(?<=［).*(?=］)"
    private const val EXACT_EJ_REGEX = ".*(?=［.*］)"
    private const val SEPARATOR_FRAGMENTS_REGEX = "[△▲･・]"
    private const val PRONUNCIATION_REGEX = "[\\p{script=Hiragana}|\\p{script=Katakana}]+" +
            "($|[\\p{script=Han}０-９]|\\d|\\s)*?"

    override fun getVocabulary(wordSource: String,
                               wordLanguageCode: String) : Vocabulary {
        val word = isolateWord(wordSource, wordLanguageCode)
        val pronunciation = isolateReading(wordSource, wordLanguageCode)
        val pitch = JapaneseVocabulary.isolatePitch(wordSource)
        return Vocabulary(word,
                          wordLanguageCode,
                          pronunciation,
                          pitch)
    }

    /**
     * Isolates the full word from the possibly messy Sanseidou html source
     * @param wordSource the raw string from the html source
     * @return The full word isolated from any furigana readings or tones
     */
    @Throws(IllegalArgumentException::class)
    private fun isolateWord(wordSource: String, wordLanguageCode: String): String {
        val cleanedWordSource = wordSource.replace(SEPARATOR_FRAGMENTS_REGEX.toRegex(), "")
        //TODO: Move use make english vocab if it is
        if (wordLanguageCode == EnglishVocabulary.LANGUAGE_CODE) {
            val ejMatcher = Pattern.compile(EXACT_EJ_REGEX).matcher(cleanedWordSource)
            if (ejMatcher.find()) {
                return ejMatcher.group(0)
            }
            return cleanedWordSource
        }
        else if (wordLanguageCode == JapaneseVocabulary.LANGUAGE_CODE) {
            val exactMatcher = Pattern
                    .compile(EXACT_WORD_REGEX)
                    .matcher(cleanedWordSource)

            return if(exactMatcher.find()) {
                exactMatcher.group(0)
            } else{
                JapaneseVocabulary.isolateWord(cleanedWordSource)
            }
        }
        throw IllegalArgumentException("Invalid Language Code: $wordLanguageCode for Sanseidou." +
                " Source: $cleanedWordSource.")
    }

    /**
     * Helper method to isolate the reading of a Japanese dictionaryEntry word from its source string.
     * @param wordSource the raw string containing the dictionaryEntry word.
     * @return a string with the isolated kana reading of the word.
     */
    private fun isolateReading(wordSource: String, wordLanguageCode: String): String {
        if (wordSource == "") {
            return ""
        }
        // Dic uses images to show pronunciations in the International Phonetic Alphabet
        // Maybe work around some other time
        if (wordLanguageCode == EnglishVocabulary.LANGUAGE_CODE) {
            var splitPosition = wordSource.indexOf('[')
            if (splitPosition < 0) {
                splitPosition = wordSource.indexOf('［')
            }
            if (splitPosition > 0) {
                return wordSource.substring(0, splitPosition)
            }
        }

        val strippedWordSource = wordSource.replace(SEPARATOR_FRAGMENTS_REGEX.toRegex(), "")
        val readingMatcher = Pattern.compile(PRONUNCIATION_REGEX).matcher(strippedWordSource)
        return if (readingMatcher.find()) {
            readingMatcher.group(0)
        } else strippedWordSource
    }




}