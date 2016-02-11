package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Created by yangr1 on 2/11/16.
 */
public class TeecValue extends TeecParameter {
    private TeecParameterTypes.Type mType;
    private int mA;
    private int mB;

    /**
     *
     * @param type only accept TEEC_VALUE_INPUT, TEEC_VALUE_OUTPUT and TEEC_VALUE_INOUT
     * @param a
     * @param b
     */
    public TeecValue(TeecParameterTypes.Type type, int a, int b){}

    public int getA(){return this.mA;}

    public int getB(){return this.mB;}

    @Override
    public TeecParameterTypes.Type getType() {
        return this.mType;
    }
}
