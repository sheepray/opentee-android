package fi.aalto.ssg.opentee.imps;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * OTValue implements the ITEEClient.IValue interface.
 */
public class OTValue implements ITEEClient.IValue {
    Flag mFlag;
    int mA;
    int mB;

    /**
     *
     * @param flag
     * @param a
     * @param b
     */
    public OTValue(Flag flag, int a, int b){
        this.mFlag = flag;
        this.mA = a;
        this.mB = b;
    }

    /**
     * Get method for private member A.
     * @return int
     */
    @Override
    public int getA(){return this.mA;}

    /**
     * Get method for private member B.
     * @return int
     */
    @Override
    public int getB(){return this.mB;}

    /**
     * Get method for flags
     * @return Value.Flag enum
     */
    public Flag getFlag(){
        return this.mFlag;
    }

    @Override
    public Type getType() {
        return Type.TEEC_PTYPE_VAL;
    }
}
