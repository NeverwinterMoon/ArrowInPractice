package com.fortysevendeg.arrowinpractice

import com.fasterxml.jackson.databind.SerializationFeature
import com.fortysevendeg.arrowinpractice.database.CharactersDatabase
import com.fortysevendeg.arrowinpractice.database.HousesDatabase
import com.fortysevendeg.arrowinpractice.endpoints.characterDetailsEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.charactersOverviewEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.charactersPerHouseEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.createOrUpdateCharacterEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.createOrUpdateHouseEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.houseDetailsEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.housesOverviewEndpoint
import com.fortysevendeg.arrowinpractice.endpoints.welcomeEndpoint
import com.fortysevendeg.arrowinpractice.error.InvalidHouseFormatException
import com.fortysevendeg.arrowinpractice.error.InvalidIdException
import com.fortysevendeg.arrowinpractice.error.NoCharactersFoundForHouse
import com.fortysevendeg.arrowinpractice.error.NotFoundException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
  val housesDB = HousesDatabase()
  val charactersDB = CharactersDatabase()

  embeddedServer(Netty, 8080) {
    enableJacksonContentNegotiation()
    setupStatusCodes()
    setupRoutes(housesDB, charactersDB)
  }.start(wait = true)
}

private fun Application.enableJacksonContentNegotiation() {
  install(ContentNegotiation) {
    jackson {
      enable(SerializationFeature.INDENT_OUTPUT) // pretty print json
    }
  }
}

private fun Application.setupStatusCodes() {
  install(StatusPages) {
    exception<InvalidIdException> { exception ->
      call.respond(HttpStatusCode.BadRequest, mapOf("error" to (exception.message ?: "")))
    }
    exception<NotFoundException> { exception ->
      call.respond(HttpStatusCode.NotFound, mapOf("error" to (exception.message ?: "")))
    }
    exception<NoCharactersFoundForHouse> { exception ->
      call.respond(HttpStatusCode.NotFound, mapOf("error" to (exception.message ?: "")))
    }
    exception<InvalidHouseFormatException> { exception ->
      call.respond(HttpStatusCode.BadRequest, mapOf("error" to (exception.message ?: "")))
    }
  }
}

private fun Application.setupRoutes(housesDB: HousesDatabase, charactersDB: CharactersDatabase) {
  routing {
    welcomeEndpoint()
    housesOverviewEndpoint(housesDB)
    houseDetailsEndpoint(housesDB)
    createOrUpdateHouseEndpoint(housesDB)
    charactersPerHouseEndpoint(charactersDB)
    charactersOverviewEndpoint(charactersDB)
    characterDetailsEndpoint(charactersDB)
    createOrUpdateCharacterEndpoint(charactersDB)
  }
}
