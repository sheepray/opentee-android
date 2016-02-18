package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * This class defines a pair of value which can be passed as a parameter for one TeecOperation.
 */
public class TeecValue extends TeecParameter {
    private Type mType;
    private int mA;
    private int mB;

    /**
     *
     * @param type only accept TEEC_VALUE_INPUT, TEEC_VALUE_OUTPUT and TEEC_VALUE_INOUT
     * @param a
     * @param b
     */
    public TeecValue(Type type, int a, int b){}

    /**
     * get method for private member A.
     * @return int
     */
    public int getA(){return this.mA;}

    /**
     * get method for private member B.
     * @return int
     */
    public int getB(){return this.mB;}

    /**
     * get method for the type of the parameter.
     * @return TeecParameter.Type
     */
    @Override
    public Type getType() {
        return this.mType;
    }
}
