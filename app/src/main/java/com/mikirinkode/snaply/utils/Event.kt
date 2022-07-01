package com.mikirinkode.snaply.utils

/*
kelas event wrapper untuk membuat
snackbar muncul sekali aja

 */

/*
    T adalah tipe generic yang bisa digunakan supaya
    kelas ini dapat membungkus berbagai macam data.
    Data yang dibungkus tersebut kemudian akan dimasukkan ke dalam variabel content.
 */
open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false
        private set

    // fungsi getContentIfNotHandled().
    // Fungsi tersebut akan memeriksa apakah aksi ini pernah dieksekusi sebelumnya.
    // Caranya yaitu dengan memanipulasi variabel hasBeenHandled.
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}