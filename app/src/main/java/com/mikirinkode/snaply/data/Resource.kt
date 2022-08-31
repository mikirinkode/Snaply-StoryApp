package com.mikirinkode.snaply.data

import java.util.*

/*
    A generic class that describes data with status
 */
class Resource<T> constructor(val status: Status, var data: T? = null, val message: String? = null, val date: Date? = null) {
    companion object {
        fun <T> success(data: T?, date: Date?): Resource<T> {
            return Resource(status = Status.SUCCESS, data = data, date = date)
        }

        fun <T> error(msg: String?, data: T?, date: Date): Resource<T> {
            return Resource(Status.ERROR, data, msg, date)
        }
    }
}

enum class Status {
    SUCCESS, ERROR, LOADING, CACHED, REAUTH, LOGOUT
}