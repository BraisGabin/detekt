package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

/**
 * The data classes are bad for the binary compatibility in public APIs. Avoid to use it.
 *
 * This rule is aimed to library maintainers. If you are developing a final application you don't need to care about
 * this issue.
 *
 * More info: https://jakewharton.com/public-api-challenges-in-kotlin/
 *
 * <noncompliant>
 * data class C(val a: String) // violation: public data class
 * </noncompliant>
 *
 * <compliant>
 * internal data class C(val a: String)
 * </compliant>
 */
class ForbiddenPublicDataClass(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "The data classes are bad for the binary compatibility in public APIs. Avoid to use it.",
        Debt.TWENTY_MINS
    )

    override fun visitClass(klass: KtClass) {
        val isPublicOrProtected = klass.visibilityModifierTypeOrDefault().let { visibility ->
            visibility != KtTokens.INTERNAL_KEYWORD && visibility != KtTokens.PRIVATE_KEYWORD
        }
        if (isPublicOrProtected) {
            if (klass.isData()) {
                report(CodeSmell(issue, Entity.from(klass.nameIdentifier ?: klass), ""))
            }
            super.visitClass(klass)
        }
    }
}
