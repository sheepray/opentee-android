package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * super class for TeecTempMemoryReference, TeecRegisteredMemoryReference and TeecValue
 */
abstract public class TeecParameter {
    abstract public TeecParameterTypes.Type getType();
}
