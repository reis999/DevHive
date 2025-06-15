package ipvc.tp.devhive.data.local

import android.content.Context
import androidx.room.*
import ipvc.tp.devhive.data.local.converter.*
import ipvc.tp.devhive.data.local.dao.*
import ipvc.tp.devhive.data.local.entity.*


@Database(
    entities = [
        UserEntity::class,
        MaterialEntity::class,
        CommentEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        StudyGroupEntity::class,
        GroupMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    AttachmentListConverter::class,
    MessageAttachmentListConverter::class,
    TimestampConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun materialDao(): MaterialDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun studyGroupDao(): StudyGroupDao
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
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}