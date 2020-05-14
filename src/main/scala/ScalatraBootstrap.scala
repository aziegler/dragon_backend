import com.ziegler.dragon._
import com.ziegler.dragon.models.{Dice, Story, Theme}
import org.scalatra._
import javax.servlet.ServletContext
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection}

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
  //  configureDb()
    val mongoClient = MongoClient()
    val codeRegistry = fromRegistries(fromProviders(classOf[Theme],classOf[Dice],classOf[Story]), DEFAULT_CODEC_REGISTRY)

    val database = mongoClient.getDatabase("dragonbackend").withCodecRegistry(codeRegistry)
    val themesColl : MongoCollection[Theme]= database.getCollection("themes")
    val diceColl : MongoCollection[Dice] = database.getCollection("dices")
    val storyColl :MongoCollection[Story] = database.getCollection("stories")
    context.setInitParameter("org.scalatra.cors.allowCredentials","false")
    context.mount(new BackendController (themesColl, diceColl, storyColl),"/*")
  }

  override def destroy(context: ServletContext): Unit = {

  }
}
