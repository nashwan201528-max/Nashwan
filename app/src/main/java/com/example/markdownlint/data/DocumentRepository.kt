package com.example.markdownlint.data

import com.example.markdownlint.model.ConfigPreset
import com.example.markdownlint.model.MarkdownDocument
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val presetDao: PresetDao
) {
    val allDocuments: Flow<List<MarkdownDocument>> = documentDao.getAllDocuments()
    val allPresets: Flow<List<ConfigPreset>> = presetDao.getAllPresets()

    suspend fun saveDocument(document: MarkdownDocument): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: MarkdownDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(id: Long) {
        documentDao.deleteById(id)
    }

    suspend fun savePreset(preset: ConfigPreset) {
        presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: ConfigPreset) {
        presetDao.deletePreset(preset)
    }
}
