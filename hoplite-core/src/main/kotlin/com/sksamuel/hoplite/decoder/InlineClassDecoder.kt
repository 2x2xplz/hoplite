package com.sksamuel.hoplite.decoder

import arrow.core.invalid
import arrow.core.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class InlineClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = when (val classifer = type.classifier) {
    is KClass<*> -> !classifer.isData &&
      classifer.primaryConstructor?.parameters?.size == 1 &&
      classifer.java.declaredMethods.any { it.name == "box-impl" }
    else -> false
  }

  override fun priority(): Int = Integer.MIN_VALUE

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Any> {

    val constructor = (type.classifier as KClass<*>).primaryConstructor?.valid()
      ?: ConfigFailure.MissingPrimaryConstructor(type).invalid()

    return constructor.flatMap { constr ->

      val param = constr.parameters[0]

      if (node is Undefined) {
        ConfigFailure.NullValueForNonNullField(node).invalid()
      } else {
        context.decoder(param)
          .flatMap { it.decode(node, param.type, context) }
          .leftMap { ConfigFailure.IncompatibleInlineType(param.type, node) }
          .map { constr.call(it) }
      }
    }
  }
}
