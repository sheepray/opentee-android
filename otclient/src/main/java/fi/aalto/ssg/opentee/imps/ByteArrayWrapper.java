package fi.aalto.ssg.opentee.imps;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * wrapper class for byte arrary to solve the issue of copying bigger array back to CA:
 */
public class ByteArrayWrapper implements Parcelable {
    byte[] mContent;

    public byte[] asByteArray(){
        return this.mContent;
    }

    ByteArrayWrapper(byte[] content){
        this.mContent = content;
    }

    protected ByteArrayWrapper(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
        Log.e("What", "Happened");

        int len = in.readInt();
        if(mContent == null || len > mContent.length){
            mContent = new byte[len];
        }
        in.readByteArray(mContent);

        Log.e("What", "Happened");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mContent.length);
        dest.writeByteArray(this.mContent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ByteArrayWrapper> CREATOR = new Creator<ByteArrayWrapper>() {
        @Override
        public ByteArrayWrapper createFromParcel(Parcel in) {
            return new ByteArrayWrapper(in);
        }

        @Override
        public ByteArrayWrapper[] newArray(int size) {
            return new ByteArrayWrapper[size];
        }
    };
}
