package data.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import data.room.context.ContextDao;
import data.room.context.ContextEntity;
import data.room.notes.NoteDao;
import data.room.notes.NoteEntity;
import data.room.rel.RelatedWordDao;
import data.room.rel.RelatedWordEntity;
import data.room.voc.VocabularyDao;
import data.room.voc.VocabularyEntity;

// TODO: What's a schema export for?
/**
 * Database object using the Room Persistence Library.
 * Singleton design to avoid multiple instances of database
 * connection when it is not needed.
 */
@Database(
        entities = {
                VocabularyEntity.class,
                RelatedWordEntity.class,
                NoteEntity.class,
                ContextEntity.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(
        {Converters.class}
)
public abstract class WanicchouDatabase extends RoomDatabase {
    public abstract VocabularyDao vocabularyDao();
    public abstract RelatedWordDao relatedWordDao();
    public abstract NoteDao noteDao();
    public abstract ContextDao contextDao();

    // Singleton to avoid multiple DB connections
    private static WanicchouDatabase INSTANCE;
    public static WanicchouDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (WanicchouDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WanicchouDatabase.class, "WanicchouDatabase").build();
                }
            }
        }
        return INSTANCE;
    }
}
