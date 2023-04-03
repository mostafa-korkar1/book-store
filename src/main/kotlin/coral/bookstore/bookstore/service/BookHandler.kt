package coral.bookstore.bookstore.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import coral.bookstore.bookstore.entity.Book
import coral.bookstore.bookstore.repository.BookRepository
import coral.bookstore.bookstore.repository.DBConnection
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext
import java.util.logging.Logger

class BookHandler(private val vertx: Vertx) {


  fun list(ctx: RoutingContext) {
    val repository = initDB(vertx)

    var bookTitle = getQueryParam(ctx, "title", "")
    var page = getQueryParam(ctx, "page", "1")
    var pageSize = getQueryParam(ctx, "pageSize", "5")

    val params = HashMap<String, String>()
    params["title"] = bookTitle
    params["page"] = page
    params["pageSize"] = pageSize

    repository.list(params)
      .onSuccess {
        ctx.response().end(Json.encodePrettily(it))
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun get(ctx: RoutingContext) {
    val repository = initDB(vertx)

    var bookId = Integer.valueOf(ctx.pathParam("id"))
    println("bookId : $bookId")
    repository.get(bookId)
      .onSuccess {
        ctx.response().end(Json.encodePrettily(it))
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun getImage(ctx: RoutingContext) {
    val repository = initDB(vertx)
    var bookId = Integer.valueOf(ctx.pathParam("id"))

  }

  fun insert(ctx: RoutingContext) {
    val repository = initDB(vertx)
    val book = mapper.readValue(ctx.body().asString(), Book::class.java)
    val fileUploadList: List<FileUpload> = ctx.fileUploads()
    for(file in fileUploadList){

    }
    repository.insert(book)
      .onSuccess {
        println("returned val : $it")
        val response = ctx.response()
        response.setChunked(true)
        response.setStatusCode(201)
        response.write("SUCCESS")
        response.end()
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun delete(ctx: RoutingContext) {
    val repository = initDB(vertx)
    var bookId = Integer.valueOf(ctx.pathParam("id"))
    repository.delete(bookId)
      .onSuccess {
        println("deleted rows : $it")
        val response = ctx.response()
        response.setChunked(true)
        response.write("SUCCESS")
        response.end()
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun update(ctx: RoutingContext) {
    val repository = initDB(vertx)
    var bookId = Integer.valueOf(ctx.pathParam("id"))
    val book = mapper.readValue(ctx.body().asString(), Book::class.java)

    repository.update(bookId, book)
      .onSuccess {
        println("updated rows : $it")
        val response = ctx.response()
        response.setChunked(true)
        response.write("SUCCESS")
        response.end()
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun exists(ctx: RoutingContext) {
    val repository = initDB(vertx)
    var isbn = ctx.pathParam("isbn")
    repository.exists(isbn)
      .onSuccess {
        println("exists : $it")
        ctx.response().end(it.toString())
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  fun setTitle(ctx: RoutingContext) {
    val repository = initDB(vertx)
    var bookId = Integer.valueOf(ctx.pathParam("id"))
    var newTitle = ctx.request().getParam("new_title")

    repository.setTitle(bookId, newTitle)
      .onSuccess {
        println("updated rows : $it")
        val response = ctx.response()
        response.setChunked(true)
        response.write("SUCCESS")
        response.end()
      }
      .onFailure { buildErrorResponse(it, ctx) }
  }

  private fun initDB(vertx: Vertx): BookRepository {
    val dbConnection = DBConnection()
    val pgPool = dbConnection.pgPool(vertx)
    return BookRepository(pgPool)
  }

  companion object {
    private val LOGGER = Logger.getLogger(BookHandler::class.java.name)
    val mapper = jacksonObjectMapper()
    val buildErrorResponse: (Throwable, RoutingContext) -> Future<Void> = { err: Throwable, ctx: RoutingContext ->
      val response = ctx.response()
      response.setChunked(true)
      response.setStatusCode(500)
      response.write("<html><body><h1>Something Error Happen</h1></body></html>")
      println("Error : $err")
      response.end()
    }

    private fun getQueryParam(ctx: RoutingContext, paramName: String, defaultValue: String): String {
      var value = ctx.request().getParam(paramName)
      if (value == null)
        return defaultValue
      else
        return value
    }

  }

}
