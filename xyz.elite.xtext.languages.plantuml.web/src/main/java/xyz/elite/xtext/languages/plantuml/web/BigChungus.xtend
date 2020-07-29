package xyz.elite.xtext.languages.plantuml.web

import java.security.Permission

/**
 * PlantUML is a *nice* piece of good ol' trusty legacy garbage code
 * that sometimes randomly tears down the entire jvm with System.exit
 * calls. Big Chungus will stop this from happening :)
 */
class BigChungus extends SecurityManager {
	override void checkExit(int status) {
		// Ain't gonna let no fool tear down the jvm
		throw new SecurityException()
	}
	
	override void checkPermission(Permission perm) {
		// Allow other activities by default
	}
}