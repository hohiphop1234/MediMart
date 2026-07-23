package com.example.medimart.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownTextTest {

    @Test
    fun parseMarkdown_unescapesNewlinesCorrectly() {
        val input = "Xin chào!\\n\\nTôi là dược sĩ MediMart.\\n1. Thuốc Paracetamol\\n2. Thuốc Ibuprofen"
        val result = parseMarkdownToAnnotatedString(input)
        val expected = "Xin chào!\n\nTôi là dược sĩ MediMart.\n1. Thuốc Paracetamol\n2. Thuốc Ibuprofen"
        assertEquals(expected, result.text)
    }

    @Test
    fun parseMarkdown_removesNullPrefix() {
        val input = "nullXin chào!\\nĐây là câu trả lời."
        val result = parseMarkdownToAnnotatedString(input)
        val expected = "Xin chào!\nĐây là câu trả lời."
        assertEquals(expected, result.text)
    }

    @Test
    fun parseMarkdown_formatsListsAndHeaders() {
        val input = "# Header 1\n- Item 1\n- Item 2"
        val result = parseMarkdownToAnnotatedString(input)
        val expected = "Header 1\n•  Item 1\n•  Item 2"
        assertEquals(expected, result.text)
    }

    @Test
    fun parseMarkdown_insertsMissingNewlinesForStructuralElements() {
        val input = "Chào bạn. ### 1. Lý do: * **Mục 1:** Nội dung * **Mục 2:** Nội dung"
        val result = parseMarkdownToAnnotatedString(input)
        val expected = "Chào bạn.\n\n1. Lý do:\n•  Mục 1: Nội dung\n•  Mục 2: Nội dung"
        assertEquals(expected, result.text)
    }
}
