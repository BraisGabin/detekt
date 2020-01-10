package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.safeAs
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtDoubleColonExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

/**
 *
 */
class UnnecessaryFullyQualifiedName(config: Config = Config.empty) : Rule(config) {

    override val defaultRuleIdAliases: Set<String> = setOf("unused")

    override val issue: Issue = Issue(
        "UnusedPrivateClass",
        Severity.Maintainability,
        "Private class is unused.",
        Debt.FIVE_MINS
    )

    override fun visit(root: KtFile) {
        super.visit(root)

        val classVisitor = UnnecessaryFullyQualifiedNameVisitor()
        root.accept(classVisitor)


    }

    private class UnnecessaryFullyQualifiedNameVisitor : DetektVisitor() {
        val namedClasses: List<Pair<KtElement, String>>
            get() = _namedClasses
        private val _namedClasses = mutableListOf<Pair<KtElement, String>>()

        override fun visitClass(klass: KtClass) {
            klass.getSuperTypeList()?.entries
                ?.mapNotNull { it.typeReference }
                ?.forEach { registerAccess(it) }
            super.visitClass(klass)
        }

        override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
            annotationEntry.typeReference?.let {
                _namedClasses.add(it to it.text)
            }
            super.visitAnnotationEntry(annotationEntry)
        }

        private fun registerAccess(typeReference: KtTypeReference) {
            // Try with the actual type of the reference (e.g. Foo, Foo?)
            typeReference.orInnerType().let { _namedClasses.add(it to it.text) }

            // Try with the type with generics (e.g. Foo<Any>, Foo<Any>?)
            val userType = typeReference.typeElement?.orInnerType() as? KtUserType
            if (userType != null) {
                 userType.referencedName?.let { _namedClasses.add(userType to it) }
            }

            // Try with the type being a generic argument of other type (e.g. List<Foo>, List<Foo?>)
            typeReference.typeElement?.typeArgumentsAsTypes
                ?.asSequence()
                ?.filterNotNull()
                ?.map { it.orInnerType() }
                ?.forEach {
                    _namedClasses.add(it to it.text)
                    // Recursively register for nested generic types (e.g. List<List<Foo>>)
                    if (it is KtTypeReference) registerAccess(it)
                }
        }

        override fun visitParameter(parameter: KtParameter) {
            parameter.typeReference?.let { registerAccess(it) }
            super.visitParameter(parameter)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            function.typeReference?.let { registerAccess(it) }
            super.visitNamedFunction(function)
        }

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            declaration.getSuperTypeList()?.entries?.forEach {
                it.typeReference?.let { registerAccess(it) }
            }
            super.visitObjectDeclaration(declaration)
        }

        override fun visitFunctionType(type: KtFunctionType) {
            type.returnTypeReference?.let { registerAccess(it) }
            super.visitFunctionType(type)
        }

        override fun visitProperty(property: KtProperty) {
            property.typeReference?.let { registerAccess(it) }
            super.visitProperty(property)
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            expression.calleeExpression?.let { _namedClasses.add(it to it.text) }
            expression.typeArguments
                .forEach {
                    _namedClasses.add(it to it.text)
                }
            super.visitCallExpression(expression)
        }

        override fun visitDoubleColonExpression(expression: KtDoubleColonExpression) {
            checkReceiverForClassUsage(expression.receiverExpression)
            if (expression.isEmptyLHS) {
                expression.safeAs<KtCallableReferenceExpression>()
                    ?.callableReference
                    ?.takeIf { looksLikeAClassName(it.getReferencedName()) }
                    ?.let { _namedClasses.add(it to it.getReferencedName()) }
            }
            super.visitDoubleColonExpression(expression)
        }

        private fun checkReceiverForClassUsage(receiver: KtExpression?) {
            (receiver as? KtNameReferenceExpression)
                ?.takeIf { looksLikeAClassName(it.text) }
                ?.let { _namedClasses.add(it to it.text) }
        }

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            checkReceiverForClassUsage(expression.receiverExpression)
            super.visitDotQualifiedExpression(expression)
        }

        // Without symbol solving it is hard to tell if this is really a class or part of a package.
        // We use "first char is uppercase" as a heuristic in conjunction with "KtNameReferenceExpression"
        private fun looksLikeAClassName(maybeClassName: String) =
            maybeClassName.firstOrNull()?.isUpperCase() == true
    }
}

/**
 * Get the non-nullable type of a reference to a potentially nullable one (e.g. String? -> String)
 */
private fun KtTypeReference.orInnerType() = (typeElement as? KtNullableType)?.innerType ?: this

/**
 * Get the non-nullable type of a type element to a potentially nullable one (e.g. String? -> String)
 */
private fun KtTypeElement.orInnerType() = (this as? KtNullableType)?.innerType ?: this
