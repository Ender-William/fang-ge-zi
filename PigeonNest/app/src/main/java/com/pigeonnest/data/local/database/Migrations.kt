package com.pigeonnest.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create family_relations table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS family_relations (
                id TEXT PRIMARY KEY NOT NULL,
                pigeon_id TEXT NOT NULL,
                father_id TEXT,
                mother_id TEXT,
                mate_id TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(pigeon_id) REFERENCES pigeons(id) ON DELETE CASCADE,
                FOREIGN KEY(father_id) REFERENCES pigeons(id) ON DELETE SET NULL,
                FOREIGN KEY(mother_id) REFERENCES pigeons(id) ON DELETE SET NULL,
                FOREIGN KEY(mate_id) REFERENCES pigeons(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_family_relations_pigeon_id ON family_relations(pigeon_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_family_relations_father_id ON family_relations(father_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_family_relations_mother_id ON family_relations(mother_id)")

        // Create pigeon_photos table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pigeon_photos (
                id TEXT PRIMARY KEY NOT NULL,
                pigeon_id TEXT NOT NULL,
                photo_path TEXT NOT NULL,
                caption TEXT,
                taken_date INTEGER,
                is_primary INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(pigeon_id) REFERENCES pigeons(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_pigeon_id ON pigeon_photos(pigeon_id)")

        // Recreate pigeons table without father_id, mother_id, mate_id
        db.execSQL(
            """
            CREATE TABLE pigeons_new (
                id TEXT PRIMARY KEY NOT NULL,
                ring_number TEXT NOT NULL,
                name TEXT NOT NULL,
                color TEXT,
                gender INTEGER NOT NULL,
                birth_date INTEGER,
                entry_date INTEGER,
                photo_path TEXT,
                loft_id TEXT,
                cage_number TEXT,
                status INTEGER NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(loft_id) REFERENCES lofts(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pigeons_ring_number ON pigeons_new(ring_number)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeons_loft_id ON pigeons_new(loft_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeons_status ON pigeons_new(status)")

        db.execSQL(
            """
            INSERT INTO pigeons_new (
                id, ring_number, name, color, gender, birth_date, entry_date,
                photo_path, loft_id, cage_number, status, notes,
                created_at, updated_at, is_deleted
            )
            SELECT 
                id, ring_number, name, color, gender, birth_date, entry_date,
                photo_path, loft_id, cage_number, status, notes,
                created_at, updated_at, is_deleted
            FROM pigeons
            """.trimIndent()
        )

        db.execSQL("DROP TABLE pigeons")
        db.execSQL("ALTER TABLE pigeons_new RENAME TO pigeons")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add eye_photo_path column to pigeons table
        db.execSQL("ALTER TABLE pigeons ADD COLUMN eye_photo_path TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add achievement column to pigeons table
        db.execSQL("ALTER TABLE pigeons ADD COLUMN achievement TEXT")
    }
}
