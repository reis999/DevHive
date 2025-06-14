package ipvc.tp.devhive.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ipvc.tp.devhive.data.local.converter.AttachmentListConverter
import ipvc.tp.devhive.data.local.converter.MapStringAnyConverter
import ipvc.tp.devhive.data.local.converter.MessageAttachmentListConverter
import ipvc.tp.devhive.data.local.converter.StringListConverter
import ipvc.tp.devhive.data.local.converter.TimestampConverter
import ipvc.tp.devhive.data.local.dao.ChatDao
import ipvc.tp.devhive.data.local.dao.CommentDao
import ipvc.tp.devhive.data.local.dao.GroupMessageDao
import ipvc.tp.devhive.data.local.dao.MaterialDao
import ipvc.tp.devhive.data.local.dao.MessageDao
import ipvc.tp.devhive.data.local.dao.StudyGroupDao
import ipvc.tp.devhive.data.local.dao.UserDao
import ipvc.tp.devhive.data.local.entity.ChatEntity
import ipvc.tp.devhive.data.local.entity.CommentEntity
import ipvc.tp.devhive.data.local.entity.GroupMessageEntity
import ipvc.tp.devhive.data.local.entity.MaterialEntity
import ipvc.tp.devhive.data.local.entity.MessageEntity
import ipvc.tp.devhive.data.local.entity.StudyGroupEntity
import ipvc.tp.devhive.data.local.entity.UserEntity


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
    version = 3,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    AttachmentListConverter::class,
    MessageAttachmentListConverter::class,
    MapStringAnyConverter::class,
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

        private val MIGRATION_1_2 = object :
            Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chats ADD COLUMN otherParticipantId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chats ADD COLUMN otherParticipantName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chats ADD COLUMN otherParticipantImageUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chats ADD COLUMN otherParticipantOnline INTEGER NOT NULL DEFAULT 0") // SQLite usa INTEGER para Boolean (0 ou 1)
                db.execSQL("ALTER TABLE chats ADD COLUMN unreadCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object :
            Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE chats ADD COLUMN participant1Name TEXT NOT NULL DEFAULT ''")
                }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devhive_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}