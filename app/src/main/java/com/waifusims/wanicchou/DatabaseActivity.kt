package com.waifusims.wanicchou

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.room.DatabaseView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.waifusims.wanicchou.ui.adapter.RelatedVocabularyAdapter
import com.waifusims.wanicchou.ui.adapter.TextBlockRecyclerViewAdapter
import com.waifusims.wanicchou.ui.adapter.TextSpanRecyclerViewAdapter
import com.waifusims.wanicchou.viewmodel.DatabaseViewModel
import com.waifusims.wanicchou.viewmodel.VocabularyViewModel
import data.room.VocabularyRepository
import data.room.entity.Vocabulary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


/**
 * Separate activity to display the related words of a SanseidoSearch.
 * If a word is long pressed, it will be searched and brought back to the home activity.
 */
class DatabaseActivity : AppCompatActivity(){
    companion object {
        private val TAG = DatabaseActivity::class.java.simpleName
        const val REQUEST_CODE = 3154
    }

    private val databaseViewModel : DatabaseViewModel by lazy {
        ViewModelProviders.of(this)
                .get(DatabaseViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)
    }

    override fun onPostResume() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_database_list)
        val observer = Observer<List<Vocabulary>>{
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.SPACE_AROUND
            recyclerView.layoutManager = layoutManager
            val onClickListener = View.OnClickListener { v ->
                Log.v(TAG, "OnClick")
                val position = recyclerView.getChildLayoutPosition(v!!)
                val vocab = databaseViewModel.vocabularyList.value!![position]
                val result = Intent()
                result.putExtra("Vocabulary", vocab)
                setResult(REQUEST_CODE, result)
                finish()
            }

            recyclerView.adapter = RelatedVocabularyAdapter(databaseViewModel.vocabularyList.value!!, onClickListener)
        }
        val obs = Observer<List<Vocabulary>>{
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.invalidate()
        }
        databaseViewModel.vocabularyList.observe(this, observer)
        databaseViewModel.vocabularyList.observe(this, obs)
        super.onPostResume()
    }
}
