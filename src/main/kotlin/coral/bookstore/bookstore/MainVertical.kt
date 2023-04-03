package coral.bookstore.bookstore

import coral.bookstore.bookstore.repository.BookRepository
import coral.bookstore.bookstore.repository.DBConnection
import coral.bookstore.bookstore.service.AuthorHandler
import coral.bookstore.bookstore.service.BookHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class MainVertical : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {


    val bookHandler = BookHandler(vertx)
    val authorHandler = AuthorHandler(vertx)

    vertx.createHttpServer()
      .requestHandler(bookRoutes(bookHandler))
//      .requestHandler(authorRoutes(authorHandler))
      .listen(8090)
      .onSuccess {
        startPromise.complete()
        println("HTTP server started on port " + it.actualPort())
      }
      .onFailure {
        startPromise.fail(it)
        println("Failed to start HTTP server:" + it.message)
      }

  }

  private fun bookRoutes(bookHandler : BookHandler) : Router {
    var router = Router.router(vertx)
    router.get("/books").handler{ ctx -> bookHandler.list(ctx)}
    router.get("/books/:id").handler{ ctx -> bookHandler.get(ctx)}
    router.get("/books/:id/image").handler{ ctx -> bookHandler.getImage(ctx)}
    router.post("/books")
      .handler(BodyHandler.create().setHandleFileUploads(true).setUploadsDirectory("./upload-images").setMergeFormAttributes(true))
      .handler{ ctx -> bookHandler.insert(ctx)}
    router.delete("/books/:id").handler{ ctx -> bookHandler.delete(ctx)}
    router.put("/books/:id").handler(BodyHandler.create()).handler{ ctx -> bookHandler.update(ctx)}
    router.head("/books/:isbn").handler{ ctx -> bookHandler.exists(ctx)}
    router.patch("/books/:id").handler{ ctx -> bookHandler.setTitle(ctx)}
    return router
  }

  private fun authorRoutes(author : AuthorHandler) : Router {
    var router = Router.router(vertx)
    router.get("/authors").handler{ ctx -> author.getAuthorBooks(ctx)}
    return router
  }

  // Optional - called when verticle is undeployed
  override fun stop() {}


}
