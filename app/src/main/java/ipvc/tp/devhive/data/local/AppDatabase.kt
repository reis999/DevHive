package ipvc.tp.devhive.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ipvc.tp.devhive.data.local.converter.AttachmentListConverter
import ipvc.tp.devhive.data.local.converter.MapStringAnyConverter
import ipvc.tp.devhive.data.local.converter.MessageAttachmentListConverter
import ipvc.tp.devhive.data.local.converter.StringListConverter
import ipvc.tp.devhive.data.local.dao.ChatDao
import ipvc.tp.devhive.data.local.dao.CommentDao
import ipvc.tp.devhive.data.local.dao.MaterialDao
import ipvc.tp.devhive.data.local.dao.MessageDao
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.local.entity.ChatEntity
import ipvc.tp.devhive.data.local.entity.CommentEntity
import ipvc.tp.devhive.data.local.entity.MaterialEntity
import ipvc.tp.devhive.data.local.entity.MessageEntity
import ipvc.tp.devhive.data.local.entity.UserEntity


@Database(
    entities = [
        UserEntity::class,
        MaterialEntity::class,
        CommentEntity::class,
        ChatEntity::class,
        MessageEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    AttachmentListConverter::class,
    MessageAttachmentListConverter::class,
    MapStringAnyConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun materialDao(): MaterialDao
    abstract fun commentDao(): CommentDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devhive_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}