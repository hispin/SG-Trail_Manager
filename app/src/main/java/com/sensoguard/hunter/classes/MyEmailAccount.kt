package com.sensoguard.hunter.classes

class MyEmailAccount {
    var emailAddress: String? = null
    var password: String? = null
    var emailServer: String? = null
    var emailPort: String? = null
    var isUseSSL: Boolean = false

    constructor(
        emailAddress: String?,
        password: String?,
        emailServer: String?,
        emailPort: String?,
        isUseSSL: Boolean
    ) {
        this.emailAddress = emailAddress
        this.password = password
        this.emailServer = emailServer
        this.emailPort = emailPort
        this.isUseSSL = isUseSSL
    }
}