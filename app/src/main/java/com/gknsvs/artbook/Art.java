package com.gknsvs.artbook;

import android.graphics.Bitmap;

public class Art {
    String name,info;
    int id,year;
    byte[] imgByte;
    Bitmap img;

    public Art(int id,String name) {
        this.name = name;
        this.id = id;
    }


}
