/**
 * From https://nodejs.org/api/https.html
 */
@file:JsModule("node:net")

package com.apollographql.mockserver


external fun createServer(requestListener: (Socket) -> Unit): Server