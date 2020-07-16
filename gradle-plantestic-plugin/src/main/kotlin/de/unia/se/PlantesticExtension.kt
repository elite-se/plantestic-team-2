package de.unia.se

import org.gradle.api.tasks.SourceSet

open class PlantesticExtension(val whenConfigAdded: (PlantesticExtension) -> SourceSet) {

    var sourceSet: SourceSet? = null

    open fun sourceSet(sourceSet : SourceSet) {
        this.sourceSet = sourceSet
        whenConfigAdded(this)
    }
}
