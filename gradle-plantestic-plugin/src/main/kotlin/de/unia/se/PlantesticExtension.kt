package de.unia.se

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet

open class PlantesticExtension(val whenConfigAdded: (PlantesticExtension) -> SourceSet) {

    var sourceSet: SourceDirectorySet? = null

    open fun sourceSet(sourceSet : SourceDirectorySet) {
        this.sourceSet = sourceSet
        whenConfigAdded(this)
    }
}
