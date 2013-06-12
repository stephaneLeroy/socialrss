package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import models.User
import play.api.i18n.Messages
import play.api.data.Form
import models._
import models.JsonFormats._
import views.html.defaultpages.todo
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.data._
import play.api.data.Forms._
import akka.event.Logging

/**
 * @author jduberville
 *
 */

object UserManager extends Controller with MongoController {
  
  
  
  val loginForm  = Form(
		 mapping(
	    "login" -> text,
	    "password" -> text
	  )(User.apply)(User.unapply)
  )
  
  def users: JSONCollection = db.collection[JSONCollection]("usersList")

  def login() = Action{
    Ok(views.html.login(Messages("login.title"), 2, loginForm))
  }
  
  def authenticate =  Action{ implicit request =>
    
    Logger.info("Begin authentication");

    val anyData = Map("login" -> "test", "password" -> "password")
    val user: User = loginForm.bindFromRequest.get
    
     val result =  Await.result(checkLoginAvailability(user.login), Duration(1, "min")).asInstanceOf[Int]
     
     result match {
     		case 0 => addUser(user)
     		case _ => {
     		  Logger.info("The login already exists");
     		  BadRequest(views.html.login(Messages("login.title"), 1, loginForm))
     		}
 		}
  } 



  def addUser(user: User)= {
    Async{
        Logger.info("Inserting user with login:" + user.login + " and password " + user.password);
        users.insert(user).map(_ => Ok(views.html.login(Messages("login.title"), 0, loginForm)))
    }
    
  }

  def checkLoginAvailability(login: String)= {
	  	  Logger.info("Checking if login:" + login + " is available");
	      val cursor: Cursor[User] = users.
	        find(Json.obj("login" -> login)).
	        sort(Json.obj("created" -> -1)).
	        cursor[User]

	      val futureUsersList: Future[List[User]] = cursor.toList
	      
	     futureUsersList.map(results =>
		  		results.count(User => true).toInt
	    	)
  }
}