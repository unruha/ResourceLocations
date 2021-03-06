package com.example.resourcelocations;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicalCenter implements Parcelable {
    private String Name;
    private String Description;
    private boolean Available;
    private double Latitude;
    private double Longitude;

    public MedicalCenter() {
        Name = "";
        Description = "";
        Available = false;
        Latitude = 0;
        Longitude = 0;
    }

    public MedicalCenter(String name, String description, boolean available, double latitude, double longitude) {
        Name = name;
        Description = description;
        Available = available;
        Latitude = latitude;
        Longitude = longitude;
    }

    protected MedicalCenter(Parcel in) {
        Name = in.readString();
        Description = in.readString();
        Available = in.readByte() != 0;
        Latitude = in.readDouble();
        Longitude = in.readDouble();
    }

    public static final Creator<MedicalCenter> CREATOR = new Creator<MedicalCenter>() {
        @Override
        public MedicalCenter createFromParcel(Parcel in) {
            return new MedicalCenter(in);
        }

        @Override
        public MedicalCenter[] newArray(int size) {
            return new MedicalCenter[size];
        }
    };

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public boolean getAvailable() {
        return Available;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setAvailable(boolean available) {
        Available = available;
    }

    public void setLatitude(double latitude)
    {
        Latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        Longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeString(Description);
        dest.writeByte((byte) (Available ? 1 : 0));
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
    }
}
