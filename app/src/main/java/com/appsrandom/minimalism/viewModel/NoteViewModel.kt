package com.appsrandom.minimalism.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsrandom.minimalism.models.Folder
import com.appsrandom.minimalism.models.Note
import com.appsrandom.minimalism.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    fun deleteAllLocks() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllLocks()
    }

    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(note)
    }

    fun deleteFolders(folder: List<Folder>) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFolders(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFolder(folder)
    }

    fun deleteNotes(query: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNotes(query)
    }

    fun deleteNotes(note: List<Note>) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNotes(note)
    }

    fun deleteFolders(query: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFolders(query)
    }

    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }

    fun insertFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertFolder(folder)
    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(note)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateFolder(folder)
    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return repository.searchNote(query)
    }

    fun getAllNotes(query: String): LiveData<List<Note>> {
        return repository.getAllNotes(query)
    }

    fun getAllFolders(query: Int): LiveData<List<Folder>> {
        return repository.getAllFolders(query)
    }

    fun getAllNotesByOldest(query: Int): LiveData<List<Note>> {
        return repository.allNotesByOldest(query)
    }

    fun getAllNotesByNewest(query: Int): LiveData<List<Note>> {
        return repository.allNotesByNewest(query)
    }

    fun getAllNotesByColor(query: Int): LiveData<List<Note>> {
        return repository.allNotesByColor(query)
    }

    fun getUnreferencedFolders(): LiveData<List<Folder>> {
        return repository.getUnreferencedFolders()
    }

    fun getUnreferencedNotes(): LiveData<List<Note>> {
        return repository.getUnreferencedNotes()
    }

    fun getAllFolderIds(): List<Int> {
        return repository.getAllFolderIds()
    }

}