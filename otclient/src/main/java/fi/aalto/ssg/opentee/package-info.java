/**
 *Open-TEE Java API Version: V 1.0<br>
 *Tested Environment: Nexus 6 (shamu build tag) running Android 5.1.1<br>
 *<p>
 *This is the main entrances of public APIs. In order to help explaining the APIs,
 * there are definitions for several essential key words in the following:<br>
 *Key word definition:<br>
 *     1. CA: Client Application that the developer is creating;<br>
 *     2. TA: Trusted Application which is already deployed in TEE.<br>
 *     3. TEE: Trusted Execution Environment in target Android device that TAs are running;<br>
 *     4. TEE Service Manager: Android service layer abstraction for TEE who is responsible for handling
 *     incoming connection from CAs and communicating with TEE with the help of NativeLibtee.<br>
 *     5. NativeLibtee: A library which enables the communication between TEE and TEE Service Manager.<br>
 *     6. Underlying library: A library which resides in the CA and communicates with the remote TEE service.<br>
 *<p>
 *<STRONG>Introduction</STRONG><br>
 *This public API documentation defines the Java APIs corresponding to the C APIs defined in
 *the GlobalPlatform Device Technology TEE Client API specification. It describes how
 *the Client Android Application communicate with the Remote TEE service manager.
 *<p>
 *<STRONG>Target audience</STRONG><br>
 *This document suits for software developers implementing:<br>
 *     1. Client Android Application running within the rich operating environment and which needs to
 *     talk to the Trusted Application.<br>
 *     2. Trusted Application running inside the TEE which need to expose its internal functions
 *     to the Client Application.
 * <p>
 *<STRONG>Background information</STRONG><br>
 *    1. what is TEE?<br>
 *    TEE stands for Trusted Execution Environment. There is another notation called Rich Execution
 *    Environment (REE). These two are often brought together to help explain both of them by comparison.
 *    Before taking a look at TEE, it is better to start explaining from REE which is more closer to our daily sense.
 *    REE stands for the common operating system along with its hardware, such as devices running Windows, Mac OSX,
 *    Linux, Android or iOS. It abstracts the underlying hardware and provides resources for the
 *    applications to run with. As such, it has rich features for applications to utilize. However, the
 *    REE frequently suffer from different kinds of attacks, such as malware, worm, trojan and ransomware.
 *    So, in order to protect very sensitive and private information such as encryption private
 *    keys against these attacks, it is good to keep these information safe in a separate container even
 *    if the REE is compromised. It is the similar notion as the safe deposit box. For instance, if bad guys broke
 *    into your home, it is still impossible for them to get all your money in the safe deposit box without
 *    the right password to open it. So, with such a thought, TEE showed up to meet such needs.
 *    Currently, the TEE shipped within devices is physically separated with REE by the hardware boundaries.
 *    Client Application (CA) runs in the REE and Trusted Application (TA) runs in the TEE.
 *    Compared with the rich features of REE, TEE mostly comes with very limited hardware capabilities.
 *
 *    <p>
 *    2. GP Specification for TEE Client API Specification.<br>
 *    GP is short for GlobalPlatform. It is a non-profit organization that publishes specifications to promote
 *    security and interoperability of secure devices. One of the specifications it published, named
 *    "GlobalPlatform Device Technology TEE Client API Specification", standardizes the ways how CAs communicate
 *    with TAs. GlobalPlatform also have other specifications for TEE but we only focus on this one specifically.
 *    The specification defines the C data types and functions for CAs to communicate with TAs.
 *    <p>
 *    3.what is Open-TEE?<br>
 *    Open-TEE is an open virtual Trusted Execution Environment which conforms to the GP TEE Specifications.
 *    For devices which are not equipped with real hardware-based TEE, it can provide a virtual TEE for
 *    developers to debug and deploy CAs and TAs before they ship applications to real TEE.
 *
 *<STRONG>Design</STRONG><br>
 *    1. What are these Java APIs and what their relationships with GP TEE Client Specification?<br>
 *    In general, these APIs are the Java version of C APIs in GP specification with a reformed design to fit
 *    Java development conventions, which mainly target on the Android devices. It can be used to
 *    develop Android Client Applications which want to utilize the functionalities that TAs provide.
 *    It provides all the necessary functionalities for CAs to communicate with remote TAs just like
 *    the C APIs defined in GP Specification.
 *    <p>
 *    2. Why they are needed?<br>
 *    In GP TEE Client Specification, it only specify the C data types and APIs which limit or complicate
 *    the development of CAs aiming for Android devices. Since Java is the mainstream programming
 *    language to develop Android applications, for Android developers who wants to utilize
 *    the GP C APIs to enable the communications between CAs and TAs, it would be a pain-in-the-ass
 *    to deal with NDK especially for those who are not familiar with it, which may result in more
 *    potential bugs and unexpected behaviours if not handled correctly. Under such a circumstance,
 *    every developers have to re-write these codes with the similar functionalities which can be a waste
 *    of efforts and are error-prone. To avoid such awkward situations, an open-sourced design, which can enable the CAs
 *    communicate with TAs while provide nice and clean public interfaces for Android developers,
 *    is urgent to conquer this issue. With such a thought, the coming public Java APIs are available
 *    for Android developers, which can release them from the burdens of dealing native development in Android.
 *    It might be not that efficient as directly dealing with C APIs but the performance should be in a tolerant level.
 *    What's more, all the implementations of public APIS are open-source for everyone. By taking advices,
 *    this shared codes can be more bug-free and efficient.
 *    <p>
 *    3. How to use it and what to expect from the APIs?<br>
 *    a. Prerequisites<br>
 *    -  The TA is deployed in Open-TEE.<br>
 *    -  The Android application which provides the remote TEE Proxy services should be running.<br>
 *    b. Check the descriptions for each API.<br>
 *<p>
 *Bug report to:<br>
 *      rui.yang at aalto.fi<br>
 *<p>
 *Organization:<br>
 *      Security System Group, Aalto University School of Science, Espoo, Finland.<br>
 *<p>
 * <STRONG>Appendix: Example code chapter</STRONG><br>
 *The following example codes demonstrate how to utilize the Java version of GP TEE Client APIs
 * to communicate with the TAs residing in the TEE.
 * <p>
 *  Firstly, we need to get an <code>ITEEClient</code> interface by calling the factory method
 *  <code>newTEEClient</code> within <code>OpenTEE</code> public factory methods wrapper so that we
 *  can obtain the APIs to interact with the remote TEE.
 *  <pre>
 *  <code>ITEEClient client = OpenTEE.newTEEClient();</code>
 *  </pre>
 *
 *  Right now, we want to establish a connection to the remote TEE Proxy service so that we can
 *  interact with the TAs running inside of TEE. By using the <code>ITEEClient</code> interface we obtained from last step,
 *  we can establish a connection to remote TEE by calling <code>initializeContext</code> method in <code>ITEEClient</code> interface.
 *  For the two input parameters, please refer to the API definition in <code>ITEEClient.IContext</code> interface.
 *  If no exception is caught, a valid <code>IContext</code> interface will be returned and program flow continues to next block of code.
 *  Otherwise, the returned <code>IContext</code> interface will be null and the exception will be threw. For different
 *  kinds of exceptions can be threw by this API, please also refer to this API definition in <code>ITEEClient.IContext</code> interface.
 *  In the handling exception code block, it is recommended to re-initializeContext again and the program
 *  flow should not continue until it get a valid <code>IContext</code> interface.
 *<pre>
 * <code>
 *   ITEEClient.IContext ctx = null;
 *  try {
 *      ctx = client.initializeContext(TEE_NAME, getApplication());
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 * After we successfully connected to the remote TEE Proxy service, in order to interact with one TA,
 * we must open a session to the TA by providing correct authentication data. To open a session,
 * the function <code>openSession</code> within <code>IContext</code> interface must be called. For
 * the input parameters for the API and possible exceptions threw by it, please refer to the API
 * definition in <code>ITEEClient.IContext</code> interface.
 *<pre>
 * <code>
 *   ITEEClient.ISession session = null;
 *  try {
 *      session = ctx.openSession(param_uuid,
 *                  param_conn_method,
 *                  param_conn_data,
 *                  param_operation);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * After successfully opened a session to a specific TA, a valid <code>ITEEClient.IContext.ISession</code> interface will be returned.
 * So we can interact with TA by using <code>invokeCommand</code> API within the <code>ITEEClient.IContext.ISession</code> interface.
 *<pre>
 * <code>
 *   try{
 *      session.invokeCommand(param_comm_id,
 *                            param_operation);
 *  }catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * In some cases, data is needed to be transferred between CA and TA. So the APIs provide two different kinds of
 * data encapsulation mechanisms. After that, they can be encapsulated again within <code>ITEEClient.IOperation</code> which can be sent to TA during <code>openSession</code> or <code>invokeCommand</code> calls.
 *
 * <p>
 * The first approach is to create an <code>ITEEClient.IValue</code> interface by calling
 * <code>newValue</code> factory method in <code>ITEEClient</code>. So up to 2 integer
 * values can be transferred. The two values are given when calling <code>newValue</code> function and
 * further interactions with this pair of values are defined in the <code>ITEEClient.IValue</code> interface.
 *<pre>
 * <code>
 *   ITEEClient.IValue iValue = client.newValue(param_flag,
 *                                              param_value_A,
 *                                              param_value_B);
 * </code>
 *</pre>
 *
 * Another approach to transfer the data is using shared memory. The notation shared memory in here works
 * as follows. Firstly, CA create a byte array as the buffer for the shared memory. Then, the CA registers
 * the byte array as a shared memory to the TA so that TA can also operate on the buffer. To create a
 * shared memory, the CA must call <code>registerSharedMemory</code> method in <code>ITEEClient.IContext</code>.
 *<pre>
 * <code>
 *   ITEEClient.ISharedMemory sharedMemory = null;
 *  try{
 *      sharedMemory = ctx.registerSharedMemory(param_byte_array,
 *                                              param_flags);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 *
 */
package fi.aalto.ssg.opentee;