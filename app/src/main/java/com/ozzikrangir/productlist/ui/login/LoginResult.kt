package com.ozzikrangir.productlist.ui.login

import com.ozzikrangir.productlist.ui.login.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)