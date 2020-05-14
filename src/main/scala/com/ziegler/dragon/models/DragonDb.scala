package com.ziegler.dragon.models

import org.mongodb.scala._
import Helpers._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Filters.elemMatch
import org.mongodb.scala.model.{Filters, ReplaceOptions, UpdateOptions}


object Theme {
  def apply(category: String, content: String, author: String): Theme =
    new Theme(new ObjectId(), category, content, author)
}

case class Theme(_id:ObjectId, category: String, content: String, author: String)

object Dice {
  def apply(color: String, faces: List[String]): Dice =
    new Dice(new ObjectId(), color, faces)
}

case class Dice(_id:ObjectId, color : String, faces : List[String])

object Story {
  def apply(theme: String, story: List[List[String]]): Story =
    new Story(new ObjectId(), theme, story, false, List())

  def apply(_id: ObjectId, theme: String, story: List[List[String]]): Story =
    new Story(_id, theme, story, false, List())

  def apply(_id: ObjectId, theme: String, story : List[List[String]],finished:Boolean):Story =
    new Story(_id,theme,story, finished,List())
}

case class Story(_id:ObjectId, theme : String, story : List[List[String]], finished : Boolean, var members : List[String])


/*
themes = ["Le cimetière en folie", "Je me suis fait.e virer de l'école des magiciens", "Je me transforme en Loup-Garou tous les lundis", "Gaël apprend à programmer"]
diceRollList = [
("Jaune", ["Quand j'étais petit", "Un beau jour", "Je connais quelqu'un ", "La semaine dernière","Tout le monde pense","Je vous ai jamais dit"]),
("Orange", ["En plus", "En réalité", "Et croyez-moi", "Et puis", "Alors moi", "Mais vous savez quoi"]),
("Rouge", ["Pas de bol","Quand soudain","Donc, sans hésiter, ","En tout cas","A mon avis", "Alors voilà"]),
("Violet", ["Coup de bol","Ni une ni deux", "A ce moment-là", "Comme par hasard", "Mais c'est pas si grave", "Figurez-vous"]),
("Bleu", ["Sauf que", "Et tenez-vous bien", "Et comme par magie", "Moralité","C'est ainsi que","Comme dirait mon pépé"])
*/



object DragonDb {

  object Dice {
    def createOrUpdate(dice:Dice, coll : MongoCollection[Dice]) : Unit = {
      val dices = coll.find(equal(("_id"), dice._id))
      if (dices.results().isEmpty)
        coll.insertOne(dice).results()
      else
        coll.replaceOne(equal(("_id"), dice._id),dice).results()
    }

    def list(coll : MongoCollection[Dice]) : List[Dice] = {
      coll.find().results().toList
    }

    def delete(dice:Dice, coll: MongoCollection[Dice]): Unit = {
      coll.deleteOne(equal(("_id"), dice._id)).results()
    }
  }

  object Story {
    def createOrUpdate(story:Story, coll : MongoCollection[Story]) : Story = {
      val stories = coll.find(equal(("_id"), story._id))
      if (stories.results().isEmpty) {
        coll.insertOne(story).results()
        story
      } else {
        coll.replaceOne(equal(("_id"), story._id),story).results()
        coll.find(equal(("_id"), story._id)).headResult()
      }
    }

    def getFirst(coll : MongoCollection[Story]) : Option[Story] = {
      coll.find(equal("finished",false)).headOptionResult()
    }

    def getForUUID(uuid:String, coll:MongoCollection[Story]) : Option[Story] = {
      coll.find(equal("members",uuid)).headOptionResult()
    }
  }

  object Theme {

    def createOrUpdate(theme:Theme, coll: MongoCollection[Theme]) : Unit =
    {
      val themes = coll.find(equal(("_id"), theme._id))
      if (themes.results().isEmpty)
          coll.insertOne(theme).results()
      else
          coll.replaceOne(equal(("_id"), theme._id),theme).results()
    }

    def list(coll: MongoCollection[Theme]) : List[Theme] = {
      coll.find().results().toList
    }

    def delete(theme:Theme, coll: MongoCollection[Theme]): Unit = {
        coll.deleteOne(equal(("_id"), theme._id)).results()
    }
  }
}

