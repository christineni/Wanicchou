package data.room

import android.app.Application
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import data.arch.search.IDictionaryWebPage
import data.arch.vocab.IVocabularyRepository
import data.room.entity.*
import data.search.SearchProvider
import data.enums.MatchType
import data.arch.vocab.WordListEntry

// TODO: Decision to search DB or online should occur here
// TODO: Make things nullable and do appropriate logic when null for everything
class VocabularyRepository(application: Application,
                           var webPage: IDictionaryWebPage,
                           private val onQueryFinish : IVocabularyRepository.OnQueryFinish)
    : IVocabularyRepository {
    //onQueryFinish might need to be a public var at some point

    val database = WanicchouDatabase.getInstance(application)

//    open class InsertEntryAsyncTask<T>(dao : BaseDao<T>)
//        : AsyncTask<T, Void, Void>() {
//        private val dao = WeakReference(dao)
//        override fun doInBackground(vararg params: T?): Void? {
//            dao.get()?.insert(params[0]!!)
//            return null
//        }
//    }
//
//    //TODO: have save results just save the results, tkae input of the vocab lists instead of dic entry.
//    // Putting it in an async task should be in original caller instead of here.
//    private class InsertDictionaryEntryAsyncTask(database : WanicchouDatabase,
//                                                 onQueryFinish: IVocabularyRepository.OnQueryFinish,
//                                                 val dictionaryEntry: DictionaryEntry,
//                                                 val relatedWords: List<WordListEntry>) :
//            AsyncTask<Void, Void, Void>() {
//        val dbReference = WeakReference(database)
//        val onQueryFinish = WeakReference(onQueryFinish)
//        override fun doInBackground(vararg params: Void?): Void? {
//
//        }
//    }
//
//    override fun saveResults(dictionaryEntry: DictionaryEntry,
//                             relatedWords: List<WordListEntry>) {
//        //Insert Vocabulary, Get Vocabulary ID, Get Dictionary ID, After both => Insert Definition
//        if (database != null){
//            var vocabulary = Vocabulary(dictionaryEntry.word,
//                    dictionaryEntry.pronunciation,
//                    dictionaryEntry.pitch,
//                    dictionaryEntry.wordLanguageCode)
//            database.vocabularyDao().insert(vocabulary)
//            val vocabularyList = database.vocabularyDao()
//                    .getLatest()
//            vocabulary = vocabularyList.value!![0]
//            val dictionaryID = database.dictionaryDao()
//                    .getDictionaryByName(dictionaryEntry.dictionary).value!!.dictionaryID
//            val definition = Definition(dictionaryEntry.definition,
//                                        dictionaryEntry.definitionLanguageCode,
//                                        dictionaryID,
//                                        vocabulary.vocabularyID)
//            database.definitionDao().insert(definition)
//            val definitionLists = mutableListOf<LiveData<List<Definition>>>()
//            for (voc in vocabularyList.value!!){
//                val definitionList = database.definitionDao()
//                        .getVocabularyDefinitions(voc.vocabularyID,
//                                                  dictionaryEntry.definitionLanguageCode)
//                definitionLists.add(definitionList)
//            }
//            for (word in relatedWords){
//                // Related words just needs to be separated better.
//                // All the information is available. I can get word, pronunciation, pitch, and langcode
//                // from the related word.
//                //What's my plan here? Since I have a uniqueness constraint, I can insert
//                // Blanks and then if they happen to
//                // search again they'll itll reinsert and replace.
//                // But there's probably a better way that involves refactoring...
//
//            }
//        }
//    }


    // TODO: Move construction of search method to activity
    // Then pass it in here
    // TODO: Create a search method factory somehow. Maybe context is sufficient?
    override fun search(searchTerm: String,
                        wordLanguageCode: String,
                        definitionLanguageCode: String,
                        matchType: MatchType,
                        onPageParsed: IDictionaryWebPage.OnPageParsed,
                        lifecycleOwner: LifecycleOwner) {
        // Do whatever, then use the callback
        val vocabularyList = searchVocabularyDatabase(searchTerm,
                                                      matchType,
                                                      wordLanguageCode,
                                                      definitionLanguageCode)
        if(vocabularyList.isEmpty()){
            webPage.search(searchTerm,
                    wordLanguageCode,
                    definitionLanguageCode,
                    matchType,
                    onPageParsed)
        }
        else {
            val currentVocabularyIndex = 0
            val definitionList = getDefinitionList(vocabularyList,
                    definitionLanguageCode,
                    currentVocabularyIndex)
            val firstMatch = vocabularyList[0]
            val relatedWords = getRelatedWords(firstMatch.vocabularyID,
                    definitionLanguageCode,
                    webPage.getDictionaryName())
            onQueryFinish.onQueryFinish(vocabularyList, definitionList, relatedWords)
        }
    }

    private fun getDefinitionList(vocabularyList: List<Vocabulary>,
                                  definitionLanguageCode: String,
                                  vocabularyIndex: Int):
            List<Definition> {
        val vocabulary = vocabularyList[vocabularyIndex]
        return database.definitionDao().getVocabularyDefinitions(vocabulary.vocabularyID,
                                                                 definitionLanguageCode)
    }

    override fun getLatest(onQueryFinish: IVocabularyRepository.OnQueryFinish) {
        val vocabularyList = database.vocabularyDao().getLatest()
        if (!vocabularyList.isEmpty()){
            val latestDefinition = database.definitionDao()
                    .getLatestDefinition(vocabularyList[0].vocabularyID)
            val dictionaryID = latestDefinition[0].dictionaryID
            val definitionLanguageCode = latestDefinition[0].languageCode
            val dictionary = getDictionaryName(dictionaryID)

            val relatedWords = SearchProvider.getWebPage(dictionary)
                    .getRelatedWords(vocabularyList, definitionLanguageCode)
            onQueryFinish.onQueryFinish(vocabularyList, latestDefinition, relatedWords)
        }
    }

    private fun getDictionaryName(dictionaryID: Int): String {
        return database.dictionaryDao().getDictionaryByID(dictionaryID).value!!.dictionaryName
    }

    fun getDefinitions(vocabularyID: Int,
                       definitionLanguageCode: String): List<Definition> {
        return database.definitionDao().getVocabularyDefinitions(vocabularyID, definitionLanguageCode)
    }

    fun getRelatedWords(vocabularyID: Int,
                        definitionLanguageCode: String,
                        dictionary: String): List<WordListEntry>{
        val webPage = SearchProvider.getWebPage(dictionary)
        val relatedVocabularyList = database.vocabularyDao().getWordsRelatedToVocabularyID(vocabularyID)

        return webPage.getRelatedWords(relatedVocabularyList, definitionLanguageCode)
    }


    private fun searchVocabularyDatabase(searchTerm: String,
                                         matchType: MatchType,
                                         wordLanguageCode: String,
                                         definitionLanguageCode: String): List<Vocabulary> {
        return when (matchType) {
            MatchType.WORD_EQUALS -> database.vocabularyDao().search(searchTerm, wordLanguageCode)
            MatchType.WORD_WILDCARDS -> database.vocabularyDao().searchWithWildcards(searchTerm, wordLanguageCode)
            MatchType.WORD_STARTS_WITH -> database.vocabularyDao().searchStartsWith(searchTerm, wordLanguageCode)
            MatchType.WORD_ENDS_WITH -> database.vocabularyDao().searchEndsWith(searchTerm, wordLanguageCode)
            MatchType.WORD_CONTAINS -> database.vocabularyDao().searchContains(searchTerm, wordLanguageCode)
            MatchType.DEFINITION_CONTAINS -> database.vocabularyDao()
                    .searchDefinitionContains(searchTerm, definitionLanguageCode)
            MatchType.DEFINITION_OR_WORD_CONTAINS -> database.vocabularyDao()
                    .searchWordOrDefinitionContains(searchTerm, wordLanguageCode, definitionLanguageCode)
        }
    }


    // I'm not too concerned about the cost of querying the DB at this
    // point since it's all local, but this could probably be improved
    private fun getTagID(tag: String) : Int {
        if (!database.tagDao().tagExists(tag)) {
            val tagEntity = Tag(tag)
            database.tagDao().insert(tagEntity)
        }
        return database.tagDao().getTag(tag).tagID
    }

    fun addTag(tag : String, word: String){
        val tagID = getTagID(tag)
        val vocabularyID = database.vocabularyDao().getVocabulary(word).vocabularyID
        val vocabularyTag = VocabularyTag(tagID, vocabularyID)
        database.vocabularyTagDao().insert(vocabularyTag)
    }

    fun deleteTag(tag: String){
        val tagEntity = database.tagDao().getTag(tag)
        database.tagDao().delete(tagEntity)
    }

    fun getVocabularyNotes(vocabularyID: Int): LiveData<List<VocabularyNote>> {
        return database.vocabularyNoteDao().getVocabularyNoteForVocabularyID(vocabularyID)
    }
}