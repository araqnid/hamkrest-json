package org.araqnid.hamkrest.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NumericNode
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test

class EquivalenceMatchersTest {
    @Test
    fun `matches string`() {
        assertThat("\"foo\"", equivalentTo("\"foo\""))
    }

    @Test
    fun `matches integer`() {
        assertThat("123", equivalentTo("123"))
    }

    @Test
    fun `matches double`() {
        assertThat("1.0", equivalentTo("1.0"))
    }

    @Test
    fun `matches boolean`() {
        assertThat("true", equivalentTo("true"))
    }

    @Test
    fun `matches null`() {
        assertThat("null", equivalentTo("null"))
    }

    @Test
    fun `empty string is not equivalent to null`() {
        assertThat("", !equivalentTo("null"))
    }

    @Test
    fun `matches simple object`() {
        assertThat("{ \"a\" : 1 }", equivalentTo("{ \"a\" : 1 }"))
    }

    @Test
    fun `item being matched must parse strictly`() {
        assertThat("{ a : 1 }", !equivalentTo("{ \"a\" : 1 }"))
    }

    @Test
    fun `reference data may parse laxly`() {
        assertThat("{ \"a\" : 1 }", equivalentTo("{ a : 1 }"))
    }

    @Test
    fun `matches Jackson tree node`() {
        val nodeFactory = JsonNodeFactory.instance
        val objectNode = nodeFactory.objectNode().apply {
            set<NumericNode>("a", nodeFactory.numberNode(1))
        }
        assertThat(objectNode, equivalentJsonNode("{ a : 1 }"))
    }
}
