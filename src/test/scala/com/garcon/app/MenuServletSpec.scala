package com.garcon.app

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class MenuServletSpec extends ScalatraSpec {
  
  def randomInt: Int = (1000 * Math.random()).toInt
  
  def is =
  "GET / on MenuServlet "                    ^
    "should return status 200"                  ! statusResult("/", 200)^
                                                end ^ p ^
  "GET /unknown on MenuServlet "             ^
    "should return status 404"                  ! statusResult("/unknown", 404)^
                                                end ^ p ^
  "GET /example on MenuServlet "             ^
    "should return status 200"                  ! statusResult("/example/" + randomInt, 200)^
                                                end

  addServlet(classOf[MenuServlet], "/*")

  def statusResult(url:String, code:Int) = 
    get(url) {
    status must_== code
  }
}
