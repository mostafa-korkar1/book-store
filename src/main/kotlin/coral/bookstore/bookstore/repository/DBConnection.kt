package coral.bookstore.bookstore.repository

import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

class DBConnection {

  fun pgPool(vertx : Vertx): PgPool {
    val connectOptions = PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("bookstore")
      .setUser("postgres")
      .setPassword("postgres")

    // Pool Options
    val poolOptions = PoolOptions().setMaxSize(5)

    // Create the pool from the data object
    return PgPool.pool(vertx, connectOptions, poolOptions)
  }

}
