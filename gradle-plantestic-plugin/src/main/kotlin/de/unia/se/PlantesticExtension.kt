package de.unia.se

import org.gradle.api.Action
import org.gradle.api.tasks.SourceSet

open class PlantesticExtension(val whenConfigAdded: (PlantesticExtension) -> SourceSet) {

//    public var sourceSet: SourceSet? = null
//
//    open fun sourceSet(action: Action<in SourceSet?>) {
//        action.execute(sourceSet)
//        whenConfigAdded(this)
//    }

    public var sourceSet: SourceSet? = null

    open fun sourceSet(sourceSet : SourceSet) {
        this.sourceSet = sourceSet
        whenConfigAdded(this)
    }
}
