package com.ramazantiftik.mapsmarker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Model implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="Place Name")
    public String placeName;

    @ColumnInfo(name="Latitude")
    public double latitude;

    @ColumnInfo(name="Longitude")
    public double longitude;

    public Model(String placeName,double latitude,double longitude){
        this.placeName=placeName;
        this.latitude=latitude;
        this.longitude=longitude;
    }

}
