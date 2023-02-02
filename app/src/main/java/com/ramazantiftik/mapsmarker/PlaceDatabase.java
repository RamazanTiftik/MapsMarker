package com.ramazantiftik.mapsmarker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Model.class},version = 1)
public abstract class PlaceDatabase extends RoomDatabase {
    public abstract PlaceDao placeDao();
}
