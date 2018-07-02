package de.unihalle.informatik.bigdata.millionsongdataset.analysis

import de.unihalle.informatik.bigdata.millionsongdataset.analysis.extensions.hadoop.*
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.hadoop.AnalysisTool
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.mapreduce.map.MapArtistSongHotttness
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.mapreduce.reader.hdf5.Hdf5SongFileInputFormat
import de.unihalle.informatik.bigdata.millionsongdataset.analysis.mapreduce.reduce.ReduceDoubleMean
import de.unihalle.informatik.bigdata.songs.extensions.containingJar
import org.apache.hadoop.io.DoubleWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import java.io.File

class AnalyseArtistSongHotttness : AnalysisTool() {

    val output: Map<String, Double>
        get() {
            return outputTabSeparated
                    .filter { it.size == 2 }
                    .map { it[0] to it[1].toDouble() }
                    .toMap()
        }

    override fun analyse(arguments: Array<String>): Boolean {
        val hdf5LibraryPath = configuration["hdf.hdf5lib.H5.hdf5lib"]
        System.setProperty("hdf.hdf5lib.H5.hdf5lib", hdf5LibraryPath)

        return jobOf(configuration) {
            jar = AnalyseArtistSongHotttness::class.containingJar
            inputPathName = this@AnalyseArtistSongHotttness.inputPathName
            inputDirRecursively = true
            inputFormatKClass = Hdf5SongFileInputFormat::class
            mapperKClass = MapArtistSongHotttness::class
            reducerKClass = ReduceDoubleMean::class
            outputKeyKClass = Text::class
            outputValueKClass = DoubleWritable::class
            outputFormatKClass = TextOutputFormat::class
            outputPathName = this@AnalyseArtistSongHotttness.outputPathName

            addCacheFile(File(hdf5LibraryPath).toURI())
        }.await(verbose = true)
    }
}