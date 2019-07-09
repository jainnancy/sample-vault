package com.knoldus

import com.typesafe.config.ConfigFactory

object ConfigurationManager {

  val config = ConfigFactory.load()

  final val url = config.getString("vault.url")
  final val uri = config.getString("vault.uri")
  final val awsStore = config.getString("vault.aws.store")
  final val vaultRootToken = config.getString("vault.root.token")
}
