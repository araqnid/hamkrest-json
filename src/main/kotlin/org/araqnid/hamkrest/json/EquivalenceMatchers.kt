package org.araqnid.hamkrest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import java.io.IOException

private val strictMapper = ObjectMapper()

private val userInputMapper = ObjectMapper()
        .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)

fun equivalentJsonNode(expectedJsonSource: String): Matcher<JsonNode> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                { it })

fun equivalentTo(expectedJsonSource: String): Matcher<String> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun equivalentTo(expectedJsonSource: ByteArray): Matcher<String> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun bytesEquivalentTo(expectedJsonSource: String): Matcher<ByteArray> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun bytesEquivalentTo(expectedJsonSource: ByteArray): Matcher<ByteArray> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun <Actual> equivalentTo(expectedJsonSource: String, jsonParser: (Actual) -> JsonNode): Matcher<Actual> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                jsonParser)

fun <Actual> equivalentTo(expectedJsonSource: ByteArray, jsonParser: (Actual) -> JsonNode): Matcher<Actual> =
        jsonRepresentationEquivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                jsonParser)

fun <Expected, Actual> jsonRepresentationEquivalentTo(expectedJsonSource: Expected, expectedJsonParser: (Expected) -> JsonNode, actualJsonParser: (Actual) -> JsonNode) : Matcher<Actual> {
    val expectedJson = try {
        expectedJsonParser(expectedJsonSource)
    } catch (e: IOException) {
        throw IllegalArgumentException("Invalid reference JSON", e)
    }

    return object : Matcher.Primitive<Actual>() {
        override fun invoke(actual: Actual): MatchResult {
            val actualJson = try {
                actualJsonParser(actual)
            } catch (e: IOException) {
                return MatchResult.Mismatch("Invalid JSON: $e")
            }

            return if (expectedJson == actualJson)
                MatchResult.Match
            else
                MatchResult.Mismatch("$actualJson")
        }

        override val description: String
            get() = "JSON $expectedJsonSource"
    }
}
