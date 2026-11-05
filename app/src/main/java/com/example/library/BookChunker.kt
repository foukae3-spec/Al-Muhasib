package com.example.library

import com.example.data.model.BookChunk

object BookChunker {
    private const val WORDS_PER_PAGE = 250
    private const val WORDS_PER_CHUNK = 100

    /**
     * Splits text into bite-sized ~100-word chunks (read in ~40 seconds)
     * aligned with 2 pages/day target (= 5 chunks daily).
     */
    fun chunkText(
        bookId: String,
        bookTitle: String,
        rawText: String,
        startIndex: Int = 0
    ): List<BookChunk> {
        val words = rawText.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val chunks = mutableListOf<BookChunk>()
        var chunkIndex = startIndex
        var wordIndex = 0

        while (wordIndex < words.size) {
            val chunkWords = words.subList(wordIndex, minOf(wordIndex + WORDS_PER_CHUNK, words.size))
            val pageNumber = (wordIndex / WORDS_PER_PAGE) + 1
            val content = chunkWords.joinToString(" ")
            val title = "$bookTitle - شذرة ${chunkIndex + 1}"
            val reflection = "تأمّل في هذا المعنى المبارك واجعله عاصماً لقلبك ودافعاً للعمل الصالح."

            chunks.add(
                BookChunk(
                    id = "${bookId}_chunk_${chunkIndex}",
                    bookId = bookId,
                    chunkIndex = chunkIndex,
                    pageNumber = pageNumber,
                    title = title,
                    content = content,
                    reflectionQuestion = reflection,
                    isRead = false,
                    readTimestamp = null
                )
            )

            wordIndex += WORDS_PER_CHUNK
            chunkIndex++
        }

        return chunks
    }
}
