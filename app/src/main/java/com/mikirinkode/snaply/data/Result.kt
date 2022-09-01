package com.mikirinkode.snaply.data

// TODO: TOLONG JELASKAN MAKSUD KELAS INI APA
sealed class Result<out R> private constructor(){
    data class Success<out T>(val data: T): Result<T>()
    data class Error(val error: String): Result<Nothing>()
    object Loading: Result<Nothing>()
}