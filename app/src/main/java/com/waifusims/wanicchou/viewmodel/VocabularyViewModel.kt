package com.waifusims.wanicchou.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import data.room.entity.Vocabulary


// TODO: Remove related words completely and just use the new scheme for queries to find same words
class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
//    var vocabularyList : LiveData<List<Vocabulary>> = getDefaultMutableLiveData()
//    var definitionList : List<LiveData<List<Definition>>> = getDefaultDefinitionList()
    private val vocabularyMediator : MediatorLiveData<List<Vocabulary>> = MediatorLiveData()
    private val wordIndexLiveData : MutableLiveData<Int> = MutableLiveData()

    init {
        vocabularyMediator.value = listOf(getDefaultVocabulary())
        wordIndexLiveData.value = 0

        vocabularyMediator.addSource(wordIndexLiveData) {
            Log.v(TAG, "Word Index: $it")
        }
    }

    fun setVocabularyList(vocabularyList: List<Vocabulary>){
        vocabularyMediator.value = vocabularyList
    }

//    private val mediator : MediatorLiveData<LiveData<Vocabulary>> = MediatorLiveData()
    fun setVocabularyObserver(lifecycleOwner: LifecycleOwner,
                              observer: Observer<List<Vocabulary>>){
        vocabularyMediator.observe(lifecycleOwner, observer)
    }

    private val currentList : List<Vocabulary>
        get() {
            return vocabularyMediator.value!!
        }

    val vocabulary : Vocabulary
    get() {
        val currentLiveDataValue = currentList
        return if (currentLiveDataValue.isNotEmpty()){
            if (wordIndex > currentLiveDataValue.size){
                wordIndexLiveData.value = 0
            }
            currentLiveDataValue[wordIndex]
        }
        else {
            getDefaultVocabulary()
        }
    }

    fun resetWordIndex(){
        wordIndexLiveData.value = 0
    }

    fun moveToPreviousWord() {
        if(wordIndex > 0){
            val currentIndex = wordIndexLiveData.value!!
            wordIndexLiveData.value = currentIndex - 1
            definitionIndex = 0
        }
    }

    fun moveToNextWord() {
        if(wordIndex < vocabularyMediator.value!!.size - 1) {
            val currentIndex = wordIndexLiveData.value!!
            wordIndexLiveData.value = currentIndex + 1
            definitionIndex = 0
        }
    }

    private var definitionIndex : Int = 0

    //TODO: Should change the list of definition/related word on vocab change.
    val wordIndex : Int
        get() = wordIndexLiveData.value!!

    private fun getDefaultRelatedWord(): List<Vocabulary>{
        return listOf(Vocabulary(DEFAULT_RELATED_WORD,
                                 DEFAULT_LANGUAGE_CODE,
                                 DEFAULT_RELATED_WORD_PRONUNCIATION))
    }

    private fun getDefaultVocabulary() : Vocabulary {
        val word = DEFAULT_WORD
        val pronunciation = DEFAULT_WORD_PRONUNCIATION
        val wordLanguageCode = DEFAULT_LANGUAGE_CODE

        return Vocabulary(word, wordLanguageCode, pronunciation)
    }


    companion object {
        private val TAG : String = VocabularyViewModel::class.java.simpleName
        private const val DEFAULT_LANGUAGE_CODE = "jp"
        private const val DEFAULT_WORD_PRONUNCIATION = "わにっちょう"
        private const val DEFAULT_WORD = "和日帳"
        private const val DEFAULT_RELATED_WORD = "テスト"
        private const val DEFAULT_RELATED_WORD_PRONUNCIATION = "てすと"
    }




//Automatically display the first entry, and related definitions/tags/etc for it
//    init {
//        vocabularyRepository.getLatest(this)
//    }

//    var tags : LiveData<List<Tag>> = vocabularyRepository.
//    var vocabularyNotes : LiveData<List<VocabularyNote>>
//            = vocabularyRepository.getVocabularyNotes()

    // Function to search, must take in the webView and all from the Activity
    // Should be void, but should initialize my object's live data and all

//    fun navigateRelatedWord(webView)
}
