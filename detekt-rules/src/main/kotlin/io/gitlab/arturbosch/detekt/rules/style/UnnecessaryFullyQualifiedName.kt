package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtPackageDirective
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

        val packageName = classVisitor.packageName
        val imports = classVisitor.imports
        classVisitor.namedClasses
            .forEach { (element, name) ->
                when {
                    packageName != null && name.startsWith(packageName.toString()) -> {
                        // TODO
                        report(CodeSmell(issue, Entity.from(element), "Unnecesary"))
                    }
                    imports.any { name.startsWith(it) } -> {
                        report(CodeSmell(issue, Entity.from(element), "Unnecesary"))
                    }
                    name.startsWith("kotlin.") -> {
                        report(CodeSmell(issue, Entity.from(element), "Unnecesary"))
                    }
                }
            }
    }

    private class UnnecessaryFullyQualifiedNameVisitor : DetektVisitor() {
        var packageName: String? = null
            private set

        var imports: List<String> = emptyList()
            private set

        val namedClasses: List<Pair<KtElement, String>>
            get() = _namedClasses
        private val _namedClasses = mutableListOf<Pair<KtElement, String>>()

        override fun visitPackageDirective(directive: KtPackageDirective) {
            packageName = directive.fqName.toString()
            super.visitPackageDirective(directive)
        }

        override fun visitImportList(importList: KtImportList) {
            imports = importList.imports.map {
                it.importPath!!.fqName.toString()
            }
            super.visitImportList(importList)
        }

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            expression.parent.let {
                if (it is KtDotQualifiedExpression || it is KtPackageDirective || it is KtImportDirective) return
            }
            _namedClasses.add(expression to expression.text)
        }

        override fun visitUserType(type: KtUserType) {
            _namedClasses.add(type to type.text)
        }
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
