// IOTConnectionInterface.aidl
package fi.aalto.ssg.opentee.sharedlibrary;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecContext;

// Declare any non-default types here with import statements

interface IOTConnectionInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


     /**
     * create a TeecContext instance
     */
     TeecContext newTeecContext();

}
