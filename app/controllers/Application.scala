package controllers

import play.api._
import play.api.mvc._
import play.api.i18n.Messages
import play.api.data.Form
import play.api.data.Forms

object Application extends Controller {
  
  def index = Action {
    request => request.session.get("connected").map{
      user => Ok(views.html.index(Messages("home.title") + " " + user))
    }.getOrElse{
    	Redirect(routes.Application.index)
    }
    
    
  }
}