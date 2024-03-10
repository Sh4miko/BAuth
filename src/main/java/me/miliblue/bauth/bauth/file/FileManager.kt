package me.miliblue.bauth.bauth.file

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class FileManager {
    companion object{
        val path = File(System.getProperty("user.dir")+File.separator+"bauth")
        fun readFile(file: String): List<String>? {
            return try {
                Files.readAllLines(Paths.get(path.toPath().toString() + "/" + file))
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun saveFile(file: String, content: String?): Boolean {
            try {
                FileWriter(path.path + "/" + file, true).use { fileWriter ->
                    fileWriter.append("\n"+content)
                    return true
                }
            } catch (e: IOException) {
                return false
            }
        }
    }
}