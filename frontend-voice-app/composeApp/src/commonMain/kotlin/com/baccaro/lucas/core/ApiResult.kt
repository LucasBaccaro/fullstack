package com.baccaro.lucas.core

// Un solo tipo para todos los resultados de API, compatible con KMP
// Solo un campo será no nulo según el resultado

data class ApiResult<T>(
    val data: T? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val networkException: Throwable? = null
) {
    companion object {
        fun <T> success(data: T): ApiResult<T> = ApiResult(data = data)
        fun <T> error(code: Int, message: String): ApiResult<T> = ApiResult(errorCode = code, errorMessage = message)
        fun <T> networkError(exception: Throwable): ApiResult<T> = ApiResult(networkException = exception)
    }
} 