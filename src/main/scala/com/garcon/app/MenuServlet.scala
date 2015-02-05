package com.garcon.app

import scala.util.Try
import org.json4s.DefaultFormats
import org.json4s.Formats
import org.scalatra.NotFound
import org.scalatra.Ok
import org.scalatra.UrlGeneratorSupport
import com.garcon.app.data.Example
import com.garcon.app.data.Wizard
import com.garcon.app.utils.Logging
import com.garcon.app.services.WizardService
import scaldi.{Injectable, Injector}

class MenuServlet(implicit inj: Injector) extends MenuappStack with UrlGeneratorSupport with Injectable {
  
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats
  
  protected val wizardService = inject [WizardService]

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Yo man, say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }
  
  get("/example/?") {
    <html>
			<body>
				<p>Please specific the <em>id</em> of the requested example</p>
			</body>
		</html>
  }
  
  val viewExample = get("/example/:id") {
    contentType = "application/json"
    
    val resource = params("id")
    val id = Try(resource.toInt).getOrElse(-1)
    
    id match {
      case -1 => NotFound(s"Resource $resource does not exist")
      case 0 => Example(0, "Nothing")
      case x => Example(x, s"Example #$x")
    } 
  }
  
  get("/redirect/example") {
    val r = (1000 * Math.random()).toInt.toString()
    redirect(url(viewExample, "id" -> r))
  }
  
  get("/wizards") {
    contentType = "application/json"
    
    Ok(wizardService.getAll())
  }

}
