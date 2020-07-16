/*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Obeo - initial API and implementation
 */
package de.unia.se.plantestic

import org.eclipse.acceleo.common.IAcceleoConstants
import org.eclipse.acceleo.common.internal.utils.AcceleoDynamicMetamodelResourceSetImpl
import org.eclipse.acceleo.common.internal.utils.AcceleoPackageRegistry
import org.eclipse.acceleo.model.mtl.MtlPackage
import org.eclipse.acceleo.model.mtl.resource.EMtlBinaryResourceFactoryImpl
import org.eclipse.acceleo.model.mtl.resource.EMtlResourceFactoryImpl
import org.eclipse.acceleo.parser.AcceleoFile
import org.eclipse.acceleo.parser.AcceleoParser
import org.eclipse.acceleo.parser.AcceleoParserProblem
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.EMFPlugin
import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.URIConverter
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.ocl.ecore.EcoreEnvironment
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory
import org.eclipse.ocl.expressions.ExpressionsPackage
import java.io.File
import java.io.IOException
import java.util.*
import java.util.jar.JarFile

/**
 * The Acceleo Compiler helper.
 *
 * @author [Stephane Begaudeau](mailto:stephane.begaudeau@obeo.fr)
 * @since 3.1
 */
class AcceleoCompiler {
    /**
     * Indicates if we should use binary resources for the serialization of the EMTL files.
     */
    var binaryResource = true

    /**
     * The source folder to compile.
     */
    protected var sourceFolder: File? = null

    /**
     * The output folder to place the compiled files.
     */
    protected var outputFolder: File? = null

    /**
     * The dependencies folders.
     */
    protected var dependencies: MutableList<File> = ArrayList()

    /**
     * The dependencies identifiers.
     */
    protected var dependenciesIDs: MutableList<String> = ArrayList()

    /**
     * The URIs of the emtl files inside the jars.
     */
    protected var jarEmtlsURI: MutableList<URI> =
        ArrayList()

    /**
     * Indicates if we should trim the position.
     *
     * @since 3.2
     */
    protected var trimPosition = false

    /**
     * The MTL file properties.
     *
     * @author [Jonathan Musset](mailto:jonathan.musset@obeo.fr)
     */
    private inner class MTLFileInfo
    /**
     * Constructor.
     */
    {
        /**
         * The IO file.
         */
        var mtlFile: File? = null

        /**
         * The absolute URI.
         */
        var emtlAbsoluteURI: URI? = null

        /**
         * The full qualified module name.
         */
        var fullModuleName: String? = null
    }

    /**
     * Sets the source folder to compile.
     *
     * @param theSourceFolder are the source folder to compile
     */
    fun setSourceFolder(theSourceFolder: String?) {
        sourceFolder = Path(theSourceFolder).toFile()
    }

    /**
     * Sets the output folder.
     *
     * @param theOutputFolder The output folder.
     */
    fun setOutputFolder(theOutputFolder: String?) {
        outputFolder = Path(theOutputFolder).toFile()
    }

    /**
     * Sets the dependencies to load before to compile. They are separated by ';'.
     *
     * @param allDependencies are the dependencies identifiers
     */
    fun setDependencies(allDependencies: String?) {
        dependencies.clear()
        val st = StringTokenizer(allDependencies, ";") //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            val path = st.nextToken().trim { it <= ' ' }
            if (path.length > 0 && !path.endsWith(JAR_EXTENSION)) {
                val parent = Path(path).removeLastSegments(1).toFile()
                if (parent != null && parent.exists() && parent.isDirectory) {
                    val segmentID = Path(path).lastSegment()
                    val candidates = parent.listFiles()
                    Arrays.sort(candidates) { o1, o2 -> -o1.name.compareTo(o2.name) }
                    var bestRequiredFolder: File? = null
                    for (candidate in candidates) {
                        if (candidate.isDirectory && candidate.name != null && candidate.name.startsWith(segmentID)
                        ) {
                            bestRequiredFolder = candidate
                            break
                        }
                    }
                    if (bestRequiredFolder != null && !dependencies.contains(bestRequiredFolder)) {
                        dependencies.add(bestRequiredFolder)
                        dependenciesIDs.add(segmentID)
                    }
                }
            } else if (path.length > 0 && path.endsWith(JAR_EXTENSION)) {
                // Let's compute the uris of the emtl files inside of the jar
                try {
                    val jarFile = JarFile(path)
                    val entries = jarFile.entries()
                    while (entries.hasMoreElements()) {
                        val nextElement = entries.nextElement()
                        val name = nextElement.name
                        if (!nextElement.isDirectory
                            && name.endsWith(IAcceleoConstants.EMTL_FILE_EXTENSION)
                        ) {
                            val jarFileURI =
                                URI.createFileURI(path)
                            val entryURI =
                                URI.createURI(name)
                            val uri = URI
                                .createURI("jar:$jarFileURI!/$entryURI") //$NON-NLS-1$//$NON-NLS-2$
                            jarEmtlsURI.add(uri)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Executes the compilation.
     */
    fun execute() {
        registerResourceFactories()
        registerPackages()
        registerLibraries()
        if (!EMFPlugin.IS_ECLIPSE_RUNNING) {
            standaloneInit()
        }
        val message = StringBuffer()
        val fileInfos: MutableList<MTLFileInfo> =
            ArrayList()
        fileInfos.addAll(computeFileInfos(sourceFolder))
        val acceleoFiles: MutableList<AcceleoFile> = ArrayList()
        val emtlAbsoluteURIs: MutableList<URI?> =
            ArrayList()
        for (mtlFileInfo in fileInfos) {
            acceleoFiles.add(AcceleoFile(mtlFileInfo.mtlFile, mtlFileInfo.fullModuleName))
            emtlAbsoluteURIs.add(mtlFileInfo.emtlAbsoluteURI)
        }
        val dependenciesURIs: MutableList<URI> =
            ArrayList()
        val mapURIs: MutableMap<URI, URI> =
            HashMap()
        computeDependencies(dependenciesURIs, mapURIs)
        computeJarDependencies(dependenciesURIs, mapURIs)
        loadEcoreFiles()
        createOutputFiles(emtlAbsoluteURIs)
        val parser =
            AcceleoParser(binaryResource, trimPosition)
        parser.parse(
            acceleoFiles,
            emtlAbsoluteURIs,
            dependenciesURIs,
            mapURIs,
            BasicMonitor()
        )
        val iterator: Iterator<AcceleoFile> = acceleoFiles.iterator()
        while (iterator.hasNext()) {
            val acceleoFile = iterator.next()
            val problems = parser.getProblems(acceleoFile)
            if (problems != null) {
                val list = problems.list
                if (!list.isEmpty()) {
                    message.append(acceleoFile.mtlFile.name)
                    message.append('\n')
                    val itProblems: Iterator<AcceleoParserProblem> = list.iterator()
                    while (itProblems.hasNext()) {
                        val problem = itProblems.next()
                        message.append(problem.line)
                        message.append(':')
                        message.append(problem.message)
                        message.append('\n')
                    }
                    message.append('\n')
                }
            }
        }
        if (message.length > 0) {
            val log = message.toString()
            throw RuntimeException(log)
        }
    }

    /**
     * This method will compute add the emtls from the jar in the list of dependencies to be loaded for the
     * compilation and map their URIs to the logical URIs used by Acceleo.
     *
     * @param dependenciesURIs The dependencies to be loaded
     * @param mapURIs          The jars URIs.
     */
    private fun computeJarDependencies(
        dependenciesURIs: MutableList<URI>,
        mapURIs: MutableMap<URI, URI>
    ) {
        for (uri in jarEmtlsURI) {
            val uriStr = uri.toString()
            val i = uriStr.indexOf("!/") //$NON-NLS-1$
            if (i > 0) {
                val fileURI = uriStr.substring(i + 2)
                var authority = uri.authority()
                val lastIndexOf = authority.lastIndexOf("/") //$NON-NLS-1$
                val indexOf = authority.lastIndexOf("_") //$NON-NLS-1$
                if (lastIndexOf > 0 && indexOf > 0) {
                    authority = authority.substring(lastIndexOf, indexOf)
                }
                val platformPluginURI =
                    URI.createPlatformPluginURI(
                        "$authority/$fileURI",
                        true
                    ) //$NON-NLS-1$
                mapURIs[uri] = platformPluginURI
                dependenciesURIs.add(uri)
            }
        }
    }

    /**
     * Create the output folders for the output files.
     *
     * @param emtlAbsoluteURIs The emtl file uris.
     */
    private fun createOutputFiles(emtlAbsoluteURIs: List<URI?>) {
        for (uri in emtlAbsoluteURIs) {
            var tmpUri = uri.toString()
            val file = "file:" //$NON-NLS-1$
            if (tmpUri.startsWith(file)) {
                tmpUri = tmpUri.substring(file.length)
            }
            if (!File(tmpUri).parentFile.exists()) {
                File(tmpUri).parentFile.mkdirs()
            }
        }
    }

    /**
     * Computes the properties of the MTL files of the given source folder.
     *
     * @param theSourceFolder the current source folder
     * @return the MTL files properties
     */
    private fun computeFileInfos(theSourceFolder: File?): List<MTLFileInfo> {
        val fileInfosOutput: MutableList<MTLFileInfo> =
            ArrayList()
        var inputPath = sourceFolder!!.absolutePath
        val file = "file:" //$NON-NLS-1$
        if (inputPath.startsWith(file)) {
            inputPath = inputPath.substring(file.length)
        }
        if (!theSourceFolder!!.exists()) {
            return fileInfosOutput
        }
        val sourceFolderAbsolutePath = theSourceFolder.absolutePath
        val mtlFiles: MutableList<File> = ArrayList()
        members(mtlFiles, theSourceFolder, IAcceleoConstants.MTL_FILE_EXTENSION)
        for (mtlFile in mtlFiles) {
            val mtlFileAbsolutePath = mtlFile.absolutePath
            if (mtlFileAbsolutePath != null) {
                var relativePath: String?
                relativePath = if (mtlFileAbsolutePath.startsWith(sourceFolderAbsolutePath)) {
                    mtlFileAbsolutePath.substring(sourceFolderAbsolutePath.length)
                } else {
                    mtlFile.name
                }
                var emtlAbsoluteURI: URI? = null
                if (outputFolder != null) {
                    var outputPath = outputFolder!!.absolutePath
                    if (outputPath.startsWith(file)) {
                        outputPath = outputPath.substring(file.length)
                    }
                    val temp =
                        Path(mtlFileAbsolutePath).removeFileExtension().addFileExtension(
                            IAcceleoConstants.EMTL_FILE_EXTENSION
                        ).toString()
                    val segments = Path(temp)
                        .matchingFirstSegments(Path(inputPath))
                    val path =
                        Path(temp).removeFirstSegments(segments)
                    val emtlPath =
                        Path(outputPath).append(path)
                    emtlAbsoluteURI = URI.createFileURI(emtlPath.toString())
                } else {
                    emtlAbsoluteURI = URI.createFileURI(
                        Path(mtlFileAbsolutePath).removeFileExtension()
                            .addFileExtension(IAcceleoConstants.EMTL_FILE_EXTENSION).toString()
                    )
                }
                val fileInfo =
                    MTLFileInfo()
                fileInfo.mtlFile = mtlFile
                fileInfo.emtlAbsoluteURI = emtlAbsoluteURI
                fileInfo.fullModuleName = AcceleoFile.relativePathToFullModuleName(relativePath)
                fileInfosOutput.add(fileInfo)
            }
        }
        return fileInfosOutput
    }

    /**
     * Computes recursively the members of the given container that match the given file extension.
     *
     * @param filesOutput is the list to create
     * @param container   is the container to browse
     * @param extension   is the extension to match
     */
    private fun members(
        filesOutput: MutableList<File>,
        container: File?,
        extension: String?
    ) {
        if (container != null && container.isDirectory) {
            val children = container.listFiles()
            if (children != null) {
                for (child in children) {
                    if (child.isFile && child.name != null && (extension == null || child.name.endsWith(".$extension"))
                    ) {
                        filesOutput.add(child)
                    } else {
                        members(filesOutput, child, extension)
                    }
                }
            }
        }
    }

    /**
     * Advanced resolution mechanism. There is sometimes a difference between how you want to load/save an
     * EMTL resource and how you want to make this resource reusable.
     *
     * @param dependenciesURIs URIs of the dependencies that need to be loaded before link resolution
     * @param mapURIs          Advanced mapping mechanism for the URIs that need to be loaded before link resolution, the
     * map key is the loading URI, the map value is the proxy URI (the real way to reuse this
     * dependency)
     */
    private fun computeDependencies(
        dependenciesURIs: MutableList<URI>,
        mapURIs: MutableMap<URI, URI>
    ) {
        val identifiersIt: Iterator<String> = dependenciesIDs.iterator()
        val dependenciesIt: Iterator<File> = dependencies.iterator()
        while (dependenciesIt.hasNext()
            && identifiersIt.hasNext()
        ) {
            val requiredFolder = dependenciesIt.next()
            val identifier = identifiersIt.next()
            if (requiredFolder != null && requiredFolder.exists() && requiredFolder.isDirectory) {
                val requiredFolderAbsolutePath = requiredFolder.absolutePath
                val emtlFiles: MutableList<File> = ArrayList()
                members(emtlFiles, requiredFolder, IAcceleoConstants.EMTL_FILE_EXTENSION)
                for (emtlFile in emtlFiles) {
                    val emtlAbsolutePath = emtlFile.absolutePath
                    val emtlFileURI =
                        URI.createFileURI(emtlAbsolutePath)
                    dependenciesURIs.add(emtlFileURI)
                    val relativePath = Path(identifier).append(
                        emtlAbsolutePath
                            .substring(requiredFolderAbsolutePath.length)
                    )
                    mapURIs[emtlFileURI] = URI.createPlatformPluginURI(relativePath.toString(), false)
                }
            }
        }
    }

    /**
     * Register the accessible ecore files.
     */
    private fun loadEcoreFiles() {
        for (requiredFolder in dependencies) {
            if (requiredFolder != null && requiredFolder.exists() && requiredFolder.isDirectory) {
                val ecoreFiles: MutableList<File> = ArrayList()
                members(ecoreFiles, requiredFolder, ECORE)
                for (ecoreFile in ecoreFiles) {
                    val ecoreURI =
                        URI.createFileURI(ecoreFile.absolutePath)
                    AcceleoPackageRegistry.INSTANCE.registerEcorePackages(
                        ecoreURI.toString(),
                        AcceleoDynamicMetamodelResourceSetImpl.DYNAMIC_METAMODEL_RESOURCE_SET
                    )
                }
            }
        }
    }

    /**
     * We may be calling for the compilation in standalone mode. In such a case we need a little more
     * initialization.
     */
    private fun standaloneInit() {
        val registry =
            Resource.Factory.Registry.INSTANCE
        if (registry.contentTypeToFactoryMap[IAcceleoConstants.BINARY_CONTENT_TYPE] == null) {
            registry.contentTypeToFactoryMap[IAcceleoConstants.BINARY_CONTENT_TYPE] = EMtlBinaryResourceFactoryImpl()
        }
        if (registry.contentTypeToFactoryMap[IAcceleoConstants.XMI_CONTENT_TYPE] == null) {
            registry.contentTypeToFactoryMap[IAcceleoConstants.XMI_CONTENT_TYPE] = EMtlBinaryResourceFactoryImpl()
        }
        registry.extensionToFactoryMap[ECORE] = EcoreResourceFactoryImpl()
        registerPackages()
    }

    /**
     * Returns the package containing the OCL standard library.
     *
     * @return The package containing the OCL standard library.
     */
    protected val oCLStdLibPackage: EPackage
        protected get() {
            val factory = EcoreEnvironmentFactory()
            val environment = factory.createEnvironment() as EcoreEnvironment
            val oclStdLibPackage = EcoreUtil.getRootContainer(
                environment.oclStandardLibrary
                    .bag
            ) as EPackage
            environment.dispose()
            return oclStdLibPackage
        }

    /**
     * This will update the resource set's package registry with all usual EPackages.
     */
    protected fun registerPackages() {
        EPackage.Registry.INSTANCE[EcorePackage.eINSTANCE.nsURI] = EcorePackage.eINSTANCE
        EPackage.Registry.INSTANCE[org.eclipse.ocl.ecore.EcorePackage.eINSTANCE.nsURI] =
            org.eclipse.ocl.ecore.EcorePackage.eINSTANCE
        EPackage.Registry.INSTANCE[ExpressionsPackage.eINSTANCE.nsURI] = ExpressionsPackage.eINSTANCE
        EPackage.Registry.INSTANCE[MtlPackage.eINSTANCE.nsURI] = MtlPackage.eINSTANCE
        EPackage.Registry.INSTANCE["http://www.eclipse.org/ocl/1.1.0/oclstdlib.ecore"] = oCLStdLibPackage
    }

    /**
     * Register the resource factories.
     */
    protected fun registerResourceFactories() {
        Resource.Factory.Registry.INSTANCE.extensionToFactoryMap[ECORE] = EcoreResourceFactoryImpl()
        Resource.Factory.Registry.INSTANCE.contentTypeToFactoryMap[IAcceleoConstants.BINARY_CONTENT_TYPE] =
            EMtlBinaryResourceFactoryImpl()
        Resource.Factory.Registry.INSTANCE.contentTypeToFactoryMap[IAcceleoConstants.XMI_CONTENT_TYPE] =
            EMtlResourceFactoryImpl()
    }

    /**
     * Register the libraries.
     */
    protected fun registerLibraries() {
        val acceleoModel = MtlPackage::class.java.protectionDomain.codeSource
        if (acceleoModel != null) {
            var libraryLocation = acceleoModel.location.toString()
            if (libraryLocation.endsWith(".jar")) { //$NON-NLS-1$
                libraryLocation = "jar:$libraryLocation!" //$NON-NLS-1$
            }
            val stdlib = MtlPackage::class.java.getResource("/model/mtlstdlib.ecore") //$NON-NLS-1$
            val resource = MtlPackage::class.java.getResource("/model/mtlnonstdlib.ecore") //$NON-NLS-1$
            URIConverter.URI_MAP[URI.createURI("http://www.eclipse.org/acceleo/mtl/3.0/mtlstdlib.ecore")] =
                URI.createURI(stdlib.toString()) //$NON-NLS-1$
            URIConverter.URI_MAP[URI.createURI("http://www.eclipse.org/acceleo/mtl/3.0/mtlnonstdlib.ecore")] =
                URI.createURI(resource.toString()) //$NON-NLS-1$
        } else {
            System.err.println("Coudln't retrieve location of plugin 'org.eclipse.acceleo.model'.") //$NON-NLS-1$
        }
    }

    companion object {
        /**
         * The ecore file extension.
         */
        protected const val ECORE = "ecore" //$NON-NLS-1$

        /**
         * The jar file extension.
         */
        private const val JAR_EXTENSION = ".jar" //$NON-NLS-1$
    }
}
