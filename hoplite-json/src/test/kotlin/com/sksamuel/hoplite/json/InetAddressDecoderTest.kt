package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.net.InetAddress

class InetAddressDecoderTest : StringSpec({
  "InetAddress decoded from json" {
    data class Test(val a: InetAddress)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_ipnet.json")
    config shouldBe Test(InetAddress.getByName("10.0.0.2"))
  }
})
