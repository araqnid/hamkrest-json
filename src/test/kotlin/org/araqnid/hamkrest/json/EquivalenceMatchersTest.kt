package org.araqnid.hamkrest.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.natpryce.hamkrest.assertion.assert
import org.junit.Test

class EquivalenceMatchersTest {
    @Test
    fun `matches string`() {
        assert.that("\"foo\"", equivalentTo("\"foo\""))
    }

    @Test
    fun `matches integer`() {
        assert.that("123", equivalentTo("123"))
    }

    @Test
    fun `matches double`() {
        assert.that("1.0", equivalentTo("1.0"))
    }

    @Test
    fun `matches boolean`() {
        assert.that("true", equivalentTo("true"))
    }

    @Test
    fun `matches null`() {
        assert.that("null", equivalentTo("null"))
    }

    @Test
    fun `empty string is not equivalent to null`() {
        assert.that("", !equivalentTo("null"))
    }

    @Test
    fun `matches simple object`() {
        assert.that("{ \"a\" : 1 }", equivalentTo("{ \"a\" : 1 }"))
    }

    @Test
    fun `item being matched must parse strictly`() {
        assert.that("{ a : 1 }", !equivalentTo("{ \"a\" : 1 }"))
    }

    @Test
    fun `reference data may parse laxly`() {
        assert.that("{ \"a\" : 1 }", equivalentTo("{ a : 1 }"))
    }

    @Test
    fun `matches Jackson tree node`() {
        val nodeFactory = JsonNodeFactory.instance
        val objectNode = nodeFactory.objectNode().apply {
            set("a", nodeFactory.numberNode(1))
        }
        assert.that(objectNode, equivalentJsonNode("{ a : 1 }"))
    }
}
