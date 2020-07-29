package xyz.elite.xtext.languages.plantuml.web

import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import javax.servlet.ServletException
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.HttpConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File
import java.nio.file.StandardOpenOption
import net.sourceforge.plantuml.Run

class PlantUMLHandler extends AbstractHandler implements Handler {
	override handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		val tmpPath = this.class.classLoader.getResource("temporary").path

		val outputPathStr = tmpPath + "/image.png"
		val outputPath = Paths.get(outputPathStr)
		if (Files.exists(outputPath)) {
			Files.delete(Paths.get(outputPathStr))
		}

		val puml = baseRequest.getParameter("puml")
		if (puml === null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
		} else {
			val pumlFile = new File(tmpPath, "image.puml")
			val pumlPath = pumlFile.path
			if (!pumlFile.exists) {
				try {
					pumlFile.createNewFile
				} catch (IOException e) {
					e.printStackTrace()
				}
			}
			val writer = Files.newBufferedWriter(Paths.get(pumlFile.absolutePath), StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)
			writer.write(puml)
			writer.close

			val String[] args = #["-failfast2", "-tpng", pumlPath]
			try {
				val chungus = new BigChungus
				System.securityManager = chungus
				System.properties.setProperty("java.awt.headless", "true")
				
				try {
					Run::main(args)
				} catch (SecurityException e) {
					// Big Chungus has prevented PlantUML from tearing down 
					// the entire JVM. Good boy :')
				}
				
				if (Files.exists(outputPath)) {
					val imageBytes = Files.readAllBytes(outputPath)
					response.setContentType("image/png")
					response.contentLength = imageBytes.length
					response.outputStream.write(imageBytes)
					response.setStatus(HttpServletResponse.SC_OK)
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
			}
		}

		if (request instanceof Request) {
			request.handled = true
		} else {
			HttpConnection.currentConnection.httpChannel.request.handled = true
		}
	}
}
