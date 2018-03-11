package com.rdc.testipc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lin Yaotian on 2018/2/10.
 */

public class Book implements Parcelable {
    private String name;

    public Book(String name){
        this.name = name;
    }

    private Book(Parcel in) {
        name = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                '}';
    }
}
