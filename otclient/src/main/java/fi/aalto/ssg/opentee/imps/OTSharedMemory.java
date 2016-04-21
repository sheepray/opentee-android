package fi.aalto.ssg.opentee.imps;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * this class implements the ISharedMemory interface
 */
public class OTSharedMemory implements ITEEClient.ISharedMemory, Parcelable {
    int mId;
    byte[] mBuffer;
    int mFlag;
    int mReturnSize = 0;    // this is used for output.

    public OTSharedMemory(byte[] buffer, int flag, int id){
        // just keep the handle.
        this.mBuffer = buffer;
        this.mFlag = flag;
        this.mId = id;
    }

    public OTSharedMemory(Parcel in){
        readFromParcel(in);
    }

    @Override
    public int getFlags() {
        return this.mFlag;
    }

    @Override
    public byte[] asByteArray(){
        return this.mBuffer;
    }

    @Override
    public int getReturnSize() {
        return this.mReturnSize;
    }

    @Override
    public int getId() {
        return this.mId;
    }

    public void setId(int id){this.mId = id;}

    public int getSize(){return mBuffer.length;}

    public void updateBuffer(byte[] newBuffer, int offset, int sizeToWrite) throws BadFormatException, ExcessDataException {
        if(newBuffer == null){
            throw new BadFormatException("new buffer is null", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }

        if( (offset + sizeToWrite) > (newBuffer.length > mBuffer.length? mBuffer.length: newBuffer.length) ){
            throw new ExcessDataException("too much data", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }

        for(int i = 0; i < sizeToWrite; i++){
            mBuffer[i + offset] = newBuffer[i + offset];
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(this.mBuffer.length);
        dest.writeByteArray(this.mBuffer);
        dest.writeInt(this.mFlag);
        dest.writeInt(this.mReturnSize);
        dest.writeInt(this.mId);
    }

    public void readFromParcel(Parcel in){
        int bl = in.readInt();
        if(this.mBuffer == null){
            this.mBuffer = new byte[bl];
        }
        in.readByteArray(this.mBuffer);
        this.mFlag = in.readInt();
        this.mReturnSize = in.readInt();
        this.mId = in.readInt();
    }

    public static final Parcelable.Creator<OTSharedMemory> CREATOR = new
            Parcelable.Creator<OTSharedMemory>() {
                public OTSharedMemory createFromParcel(Parcel in) {
                    return new OTSharedMemory(in);
                }

                public OTSharedMemory[] newArray(int size) {
                    return new OTSharedMemory[size];
                }
            };

}
