package de.unihalle.informatik.bigdata.millionsongdataset.analysis.mapreduce.reader.hdf5

import de.unihalle.informatik.bigdata.millionsongdataset.analysis.extensions.hadoop.absolutePath
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.extensions.hdf5.openHdf5File
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.extensions.hdf5.songs
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.model.Song
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.InputSplit
import org.apache.hadoop.mapreduce.RecordReader
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.hadoop.mapreduce.lib.input.FileSplit
import java.io.IOException

class Hdf5SongFileRecordReader : RecordReader<Text, Song>() {

    private lateinit var fileSplit: FileSplit
    private lateinit var configuration: Configuration
    private var processed = false

    private lateinit var key: Text
    private lateinit var value: Song

    override fun initialize(inputSplit: InputSplit, taskAttemptContext: TaskAttemptContext) {
        this.fileSplit = inputSplit as FileSplit
        this.configuration = taskAttemptContext.configuration
    }

    @Throws(IOException::class)
    override fun nextKeyValue(): Boolean {
        if (!processed) {
            openHdf5File(fileSplit.path.absolutePath) {
                val songs = songs
                if (songs.size > 1) {
                    println("Note that we'll just parse song information for the first song in each file.")
                }

                val song = songs[0]

                // TODO Parse all songs in a track file.

                key = Text(song.track.id)
                value = song

            }

            processed = true
            return true
        }
        return false
    }

    override fun getCurrentKey(): Text = key

    override fun getCurrentValue() = value

    override fun getProgress() = if (processed) 1.0f else 0.0f

    override fun close() = Unit // Nothing to close.
}