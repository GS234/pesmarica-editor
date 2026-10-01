package org.mpp.dbuild.data.sources

import pesmaricaeditor.shared.generated.resources.Res


class DatabaseInitService(
    private val fileCopyInterface: FileCopyInterface
) {
    suspend fun initDb(destinationPath: String){
        // fileCopyInterface.copyTo(initFilePath, destinationPath)
        val bytes = Res.readBytes("files/init")
        fileCopyInterface.saveTo(bytes, destinationPath)
    }
}

// interface
interface FileCopyInterface{
    suspend fun copyTo(sourcePath: String, destinationPath: String)
    suspend fun saveTo(byteArray: ByteArray, destinationPath: String)
}