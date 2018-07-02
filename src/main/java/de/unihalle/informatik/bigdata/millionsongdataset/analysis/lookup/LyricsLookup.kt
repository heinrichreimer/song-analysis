package de.unihalle.informatik.bigdata.millionsongdataset.analysis.lookup

import de.unihalle.informatik.bigdata.millionsongdataset.analysis.model.Lyrics
import java.io.File

class LyricsLookup(private val lookupFile: File) : AbstractMap<String, LyricsLookup.Entry>() {

    constructor(lookupFilePath: String) : this(File(lookupFilePath))

    constructor(dataset: Dataset = Dataset.TEST) : this(dataset.lookupTablePath)

    override val entries: Set<Entry> = parseEntries()

    private fun parseEntries(): Set<Entry> {
        val lines = lookupFile.readLines()
        val linesUntilWordList = lines
                .dropWhile { line ->
                    line.startsWith(commentLinePrefix) || !line.startsWith(wordListLinePrefix)
                }
        val words = linesUntilWordList
                .take(1)
                .single()
                .removePrefix(wordListLinePrefix.toString())
                .split(wordListDelimiter)
        return linesUntilWordList
                .dropWhile { line ->
                    line.startsWith(commentLinePrefix) || line.startsWith(wordListLinePrefix)
                }
                .mapTo(mutableSetOf()) { line ->
                    val columns = line
                            .split(countListDelimiter)
                    val trackId = columns[0]
                    val musixmatchTrackId = columns[1]
                    val lyrics: Lyrics = columns
                            .drop(2)
                            .map { column ->
                                val wordCount = column.split(countListElementDelimiter)
                                val word = words[wordCount[0].toInt() - 1]
                                val count = wordCount[1].toInt()
                                word to count
                            }
                            .toMap()
                    Entry(trackId, musixmatchTrackId, lyrics)
                }
    }


    companion object {
        const val commentLinePrefix = '#'
        const val wordListLinePrefix = '%'
        const val wordListDelimiter = ','
        const val countListDelimiter = ','
        const val countListElementDelimiter = ':'

        private const val testLookupTablePath = "data/lyrics/mxm_dataset_test.txt"
        private const val trainLookupTablePath = "data/lyrics/mxm_dataset_train.txt"
        const val defaultLookupTablePath = testLookupTablePath
    }

    enum class Dataset(val lookupTablePath: String) {
        TEST(testLookupTablePath),
        TRAIN(trainLookupTablePath)
    }

    data class Entry(
            val trackId: String,
            val musixmatchTrackId: String,
            val lyrics: Lyrics
    ) : Map.Entry<String, Entry> {
        override val key: String = trackId
        override val value: Entry = this
    }
}