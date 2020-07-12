/*
 * generated by Xtext 2.16.0
 */
package xyz.elite.xtext.languages.plantuml.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.Test
import org.junit.Assert
import org.junit.runner.RunWith
import xyz.elite.xtext.languages.plantuml.plantUML.Model
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets

@RunWith(XtextRunner)
@InjectWith(PlantUMLInjectorProvider)
class PlantUMLParsingTest {
	@Inject
	ParseHelper<Model> parseHelper

	def String loadPUML(String name) {
	    val uri = this.getClass().getResource("/" + name + ".puml")
        return String.join("\n", Files.readAllLines(Paths.get(uri.toURI()), StandardCharsets.UTF_8))
	}

    def void parsesWithoutErrors(String pumlName) {
        val result = parseHelper.parse(loadPUML(pumlName))
        Assert.assertNotNull(result)
        val errors = result.eResource.errors
        Assert.assertTrue('''Unexpected errors: «errors.join(", ")»''', errors.isEmpty)
    }

	@Test
    def void empty() {
        val result = parseHelper.parse(loadPUML("empty"))
        if (result !== null) {
            val errors = result.eResource.errors
            Assert.assertFalse('''An empty PUML file should cause errors!''', errors.isEmpty);
        }
    }

    @Test
    def void minimalFailing() {
        val result = parseHelper.parse(loadPUML("failing"))
        Assert.assertNotNull(result)
		val errors = result.eResource.errors
		print(errors)
        Assert.assertFalse('''Errors: «errors.join(", ")»''', errors.isEmpty);
    }

    @Test
    def void garbageLineShouldFail() {
        val result = parseHelper.parse(loadPUML("garbage-line"))
        Assert.assertNotNull(result)
		val errors = result.eResource.errors
        Assert.assertFalse('''Errors: «errors.join(", ")»''', errors.isEmpty);
    }

    @Test
    def void minimal() {
        parsesWithoutErrors("minimal")
    }

    @Test
    def void arrows() {
        parsesWithoutErrors("arrows")
    }

    @Test
    def void colorfulArrows() {
        parsesWithoutErrors("colorful-arrows")
    }

    @Test
    def void autonumber() {
        parsesWithoutErrors("autonumber")
    }

    @Test
    def void pageMetaInfo() {
        parsesWithoutErrors("page-meta-info")
    }

    @Test
    def void arbitraryDescription() {
        parsesWithoutErrors("arbitrary-description")
    }    

    @Test
    def void groups() {
        parsesWithoutErrors("groups")
    }

    @Test
    def void notes() {
        parsesWithoutErrors("notes")
    }

    @Test
    def void divider() {
        parsesWithoutErrors("divider")
    }

	@Test
	def void allFeatures() {
        parsesWithoutErrors("all-features")
	}
}
