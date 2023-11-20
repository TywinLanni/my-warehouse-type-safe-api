package com.github.tywinlanni.mywarehouse.core

import io.ktor.client.*

interface MyWarehouse {
    val client: HttpClient

    interface Mutation : MyWarehouse

    interface Retrieve : MyWarehouse
}
