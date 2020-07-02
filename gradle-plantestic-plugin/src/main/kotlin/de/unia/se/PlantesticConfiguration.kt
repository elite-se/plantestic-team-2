package de.unia.se;

import org.gradle.api.Named
import org.gradle.api.tasks.SourceSet

import javax.annotation.Nonnull


class PlantesticConfiguration(private val name: String, private val sourceSet: SourceSet) : Named {
    @Nonnull
    override fun getName(): String {
        return name
    }
}
