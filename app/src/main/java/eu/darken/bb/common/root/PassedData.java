package eu.darken.bb.common.root;

import android.os.Parcel;
import android.os.Parcelable;

public class PassedData implements Parcelable {
    // These are called by the framework to deserialize a Binder transaction into objects
    public static final Parcelable.Creator<PassedData> CREATOR = new Parcelable.Creator<PassedData>() {
        public PassedData createFromParcel(Parcel in) {
            return new PassedData(in);
        }

        public PassedData[] newArray(int size) {
            return new PassedData[size];
        }
    };

    // Constructor called by CREATOR, our custom deserializer
    private PassedData(Parcel p) {
        this.a = p.readInt();
        this.b = p.readLong();
        this.c = p.readString();
    }

    // Our custom serializer
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(a);
        dest.writeLong(b);
        dest.writeString(c);
    }

    @Override
    public int describeContents() {
        // return Parcelable.CONTENTS_FILE_DESCRIPTOR here if you have FileDescriptor fields
        return 0;
    }

    // Data class members as usual

    private final int a;
    private final long b;
    private final String c;

    public PassedData(int a, long b, String c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int getA() {
        return a;
    }

    public long getB() {
        return b;
    }

    public String getC() {
        return c;
    }
}