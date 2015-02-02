import org.scalatra.LifeCycle

import com.garcon.app.MenuServlet

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    implicit val injector = Global.applicationModule
    
    context.mount(new MenuServlet(), "/*")
  }
}
