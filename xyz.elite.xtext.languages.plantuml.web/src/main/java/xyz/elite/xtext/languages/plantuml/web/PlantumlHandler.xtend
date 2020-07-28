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

class PlantumlHandler extends AbstractHandler implements Handler {
	override handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		val tmpPath = this.class.classLoader.getResource("tmp").path
		
		val outputPath = tmpPath + "/image.png"
		
		val puml = baseRequest.getParameter("puml")
		val pumlPath = tmpPath + "/image.puml"
		val pumlFile = new File(tmpPath, "image.puml")
		if (!pumlFile.exists) {
			try {
				pumlFile.createNewFile
			} catch (IOException e) {
				e.printStackTrace()
			}
		}
		val writer = Files.newBufferedWriter(Paths.get(pumlFile.absolutePath), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
		val split = puml.split("\\\\n")
		split.forEach[ e |
			writer.write(e)
			writer.newLine
		]
		writer.close

		val plantumlPath = class.classLoader.getResource("plantuml.jar").path
		val ps = Runtime.runtime.exec(#["java", "-jar", plantumlPath, "-tpng", pumlPath])
		ps.waitFor
		
		// Print process stdout and stderr
		val is = ps.inputStream
		val es = ps.errorStream
		if (is.available > 0) {
			var b = newByteArrayOfSize(is.available)
			is.read(b, 0, b.length);
			System.out.println(new String(b))
        }
		if (es.available > 0) {
			var b = newByteArrayOfSize(es.available)
			es.read(b, 0, b.length)
			System.err.println(new String(b))
        }
		
		val imageBytes = Files.readAllBytes(Paths.get(outputPath))
		response.setContentType("image/png")
		response.contentLength = imageBytes.length
		response.outputStream.write(imageBytes)
		response.setStatus(HttpServletResponse.SC_OK)
		
		if (request instanceof Request) {
			request.handled = true
		} else {
			HttpConnection.currentConnection.httpChannel.request.handled = true
		}
	}
}