/* 
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package org.lwjgl.generator

import java.util.HashMap

abstract class TemplateElement {

	class object {
		private val EMPTY_MODIFIERS: MutableMap<Class<out TemplateModifier>, TemplateModifier> = HashMap(0)
	}

	private var modifiers = EMPTY_MODIFIERS

	fun setModifiers(vararg modifiers: TemplateModifier) {
		modifiers forEach {
			it.validate(this)
		}

		this.modifiers = HashMap(modifiers.size)

		modifiers.forEach {
			val old = this.modifiers.put(it.javaClass, it)
			if ( old != null )
				throw IllegalArgumentException("Template modifier ${it.javaClass.getSimpleName()} specified more than once.")
		}
	}

	fun has(modifier: TemplateModifier) = has(modifier.javaClass)
	fun has(modifier: Class<out TemplateModifier>) = modifiers.containsKey(modifier)
	fun <T: TemplateModifier> get(modifier: Class<T>): T = modifiers[modifier] as T

	/** Returns true if the parameter has a ReferenceModifier with the specified reference. */
	fun hasRef(modifier: Class<out ReferenceModifier>, reference: String): Boolean {
		val mod = modifiers[modifier]
		return mod != null && (mod as ReferenceModifier).reference equals reference
	}

	fun hasSpecialModifier() = modifiers.values().find { it.isSpecial } != null

}

/** A template modifier. Replaces the annotations in the pre-3.0 generator. */
public trait TemplateModifier {
	/** When true, this modifier requires special Java-side handling. */
	val isSpecial: Boolean

	/** Implementations should check that the specified template type is valid for this modifier. */
	fun validate(ttype: TemplateElement)
}

/** A TemplateModifier with a reference to another TemplateElement. */
public abstract class ReferenceModifier(val reference: String): TemplateModifier {
	override val isSpecial: Boolean = true
}

// DSL extensions

public fun <T: TemplateElement> TemplateModifier._(element: T): T {
	element.setModifiers(this)
	return element
}

public fun mods(vararg modifiers: TemplateModifier): Array<TemplateModifier> = modifiers
public fun <T: TemplateElement> Array<TemplateModifier>._(element: T): T {
	element.setModifiers(*this)
	return element
}