/**
 * Open-TEE Java API Version: V 1.0<br>
 * Tested Environment: Nexus 6 (shamu build tag) running Android 5.1.1<br>
 *
 * Introduction:<br>
 *     This public API documentation defines the Java APIs corresponding to the C APIs defined in
 *     the GlobalPlatform Device Technology TEE Client API specification \ref{}. It describes how
 *     the Client Android Application communicate with the Remote TEE service manager
 * This is the main entrances of public APIs. In order to help explaining the APIs,
 * there are definitions for several essential key words in the following:<br>
 * Key word definition:<br>
 *     1. CA: Client Application that the developer is creating;<br>
 *     2. TA: Trusted Application which is already deployed in TEE.<br>
 *     3. TEE: Trusted Execution Environment in target Android device that TAs are running;<br>
 *     4. TEE Service Manager: Android service layer abstraction for TEE who is responsible for handling
 *     incoming connection from CAs and communicating with TEE with the help of NativeLibtee.<br>
 *     5. NativeLibtee: A library which enables the communication between TEE and TEE Service Manager.<br>
 *     6. Underlying library: A library which resides in the CA and communicates with the remote TEE service.<br>
 *<p>
 * Target audience:<br>
 * This document suits for software developers implementing:<br>
 *     1. Client Android Application running within the rich operating environment and which needs to
 *     talk to the Trusted Application.<br>
 *     2. Trusted Application running inside the TEE which need to expose its internal functions
 *     to the Client Application.
 * <p>
 * Background information<br>
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
 * Design:<br>
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
 *      a. How to set up the environment.<br>
 *          [Prerequisites]<br>
 *          A. TAs are deployed in Open-TEE.<br>
 *          B. The Android Application which provides the remote TEE services should be running.<br>
 *      b. Check the descriptions for each API.<br>
 *
 * Bug report to:<br>
 *      rui.yang at aalto.fi<br>
 * Organization:<br>
 *      Platform Security of SSG group, Aalto University School of Science, Espoo, Finland.<br>
 */
package fi.aalto.ssg.opentee;