/*
 * generated by Xtext 2.16.0
 */
package xyz.elite.xtext.languages.plantuml.ide

import com.google.inject.Guice
import org.eclipse.xtext.util.Modules2
import xyz.elite.xtext.languages.plantuml.PlantUMLRuntimeModule
import xyz.elite.xtext.languages.plantuml.PlantUMLStandaloneSetup

/**
 * Initialization support for running Xtext languages as language servers.
 */
class PlantUMLIdeSetup extends PlantUMLStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new PlantUMLRuntimeModule, new PlantUMLIdeModule))
	}
	
}
