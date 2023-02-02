package com.ramazantiftik.mapsmarker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    @Query("select * from Model")
    Flowable<List<Model>> getAll();

    @Delete
    Completable delete(Model model);

    @Insert
    Completable insert(Model model);

}
