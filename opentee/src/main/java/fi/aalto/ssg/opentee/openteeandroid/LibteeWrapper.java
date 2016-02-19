/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee.openteeandroid;

/**
 * wrapper class for libtee
 */
public class LibteeWrapper{

    static {
        System.loadLibrary(OTJniConstants.LIBTEE_WRAPPER_MODULE_NAME);
    }

    /**
     * native functions section
     */

    /**
     *
     * @param name specifies the name of the TEE to connect to.
     * @return TeecResult
     */
    public static synchronized native int teecInitializeContext(String name);

}