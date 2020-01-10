package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class UnnecessaryFullyQualifiedNameSpec : Spek({

    val subject by memoized { UnnecessaryFullyQualifiedName() }

    describe("same package") {
        it("asReceiver") {
            val code = """
                import B.G
                
                fun test() {
                    B.G.let { it }
                }
                
                class B {
                    object G
                }
                
                class C {
                    object G
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("asReceiverProperty") {
            val code = """
                package inspector.p30879
                import inspector.p30879.C.G
                
                val <T> T.letVar: Int; get() = 0
                
                fun test() {
                    C.G.letVar
                }
                
                class B { object G }
                class C { object G }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("classLiteral") {
            val code = """
                // WITH_RUNTIME
                package foo
                
                class Foo
                
                fun bar() {
                    foo.Foo::class
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("classLiteral2") {
            val code = """
                // WITH_RUNTIME
                package foo
                
                class Foo
                
                fun bar() {
                    foo.Foo::class.java
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("classLiteral3") {
            val code = """
                // WITH_RUNTIME
                // DISABLE-ERRORS
                package foo.www.ddd
                
                class Check {
                    class BBD {
                        class Bwd {
                            fun dad() {
                                val Bwd = 42
                
                                class BBD
                
                                val a = foo.www.ddd.Check.BBD.Bwd::class.java.annotatedInterfaces.size
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("classLiteral4") {
            val code = """
                // WITH_RUNTIME
                // DISABLE-ERRORS
                package foo.www.ddd
                
                class Check {
                    class BBD {
                        class Bwd {
                            fun dad() {
                                fun Bwd(): String = ""
                                val a = foo.www.ddd.Check.BBD.Bwd::class.java.annotatedInterfaces.size
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("classLiteral5") {
            val code = """
                // WITH_RUNTIME
                // DISABLE-ERRORS
                package foo.www.ddd
                
                class Check {
                    class BBD {
                        class Bwd {
                            fun dad() {
                                class Bwd
                                val a = foo.www.ddd.Check.BBD.Bwd::class.java.annotatedInterfaces.size
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionCollision") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                open class SuperClass {
                    companion object {
                        fun check() {}
                    }
                }
                
                class Child : SuperClass() {
                    class Foo constructor() {
                        constructor(i: Int) : this()
                        fun check() {}
                
                        fun foo() {
                            my.simple.name.SuperClass.check()
                            Child.Foo.check()
                        }
                
                        companion object {
                            fun check() {}
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionOnClass") {
            val code = """
                package my.simple.name
                import my.simple.name.Foo.Companion.VARIABLE
                
                class Foo {
                    companion object {
                        const val VARIABLE = 1
                    }
                }
                
                fun main() {
                    val a = my.simple.name.Foo.VARIABLE
                    val b = my.simple.name.Foo.Companion.VARIABLE
                    val c = my.simple.name.Foo()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("companionOnVariable") {
            val code = """
                package my.simple.name
                import my.simple.name.Foo.Companion.VARIABLE
                
                class Foo {
                    companion object {
                        const val VARIABLE = 1
                    }
                }
                
                fun main() {
                    val a = my.simple.name.Foo.VARIABLE
                    val b = my.simple.name.Foo.Companion.VARIABLE
                    val c = my.simple.name.Foo()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("companionType") {
            val code = """
                package my.simple.name
                
                
                class Bar {
                    class Foo {
                        companion object {
                            class CheckClass
                        }
                    }
                }
                
                
                fun foo(a: my.simple.name.Bar.Foo.Companion.CheckClass) {
                
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionType2") {
            val code = """
                package my.simple.name
                
                import my.simple.name.Bar.Foo.Companion.CheckClass
                
                class Bar {
                    class Foo {
                        companion object {
                            class CheckClass
                        }
                    }
                }
                
                
                fun foo(a: my.simple.name.Bar.Foo.Companion.CheckClass) {
                
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionType3") {
            val code = """
                package my.simple.name
                
                import my.simple.name.Bar.Foo.Companion.CheckClass
                
                class Bar {
                    class Foo {
                        companion object {
                            class CheckClass
                        }
                    }
                }
                
                class F {
                    class CheckClass
                
                    fun foo(a: my.simple.name.Bar.Foo.Companion.CheckClass) {
                
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionType4") {
            val code = """
                package my.simple.name
                
                import my.simple.name.Bar.Foo.Companion.CheckClass
                
                class Bar {
                    class Foo {
                        companion object {
                            class CheckClass
                        }
                    }
                }
                
                class F {
                    fun foo(a: my.simple.name.Bar.Foo.Companion.CheckClass) {}
                
                    companion object {
                        class CheckClass
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionType5") {
            val code = """
                package my.simple.name
                
                class F {
                    class CheckClass
                
                    fun foo(a: F.CheckClass) {}
                
                    companion object {
                        class CheckClass
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("companionWithOuterName") {
            val code = """
                package my.sample
                
                class Inner {
                    fun a() {
                        my.sample.Inner.say()
                    }
                
                    companion object Inner {
                        fun say() {}
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("enumEntry") {
            val code = """
                // WITH_RUNTIME
                import Encoding.MJPEG
                
                class Player {
                    val status: String = Encoding.MJPEG.toString()
                }
                
                enum class Encoding {
                    UNKNOWN,
                    MJPEG,
                    H264
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("enumInEnum") {
            val code = """
                // WITH_RUNTIME
                enum class B() {
                    ;
                
                    fun test() {
                        B.values()
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("enumInEnum2") {
            val code = """
                // WITH_RUNTIME
                enum class B(val x: Int) {
                    BB(B.values().size)
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("expression") {
            val code = """
                package my.simple.name
                
                class Foo
                class Bar
                
                fun main() {
                    val a = my.simple.name.Foo()
                    val b = my.simple.name.Bar()
                    val c = my.simple.name.Foo()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("expression2") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                fun main() {
                    val a = kotlin.Int.MAX_VALUE
                    val b = kotlin.Int.Companion.MAX_VALUE
                    val c = kotlin.Int.Companion::MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("expression3") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                fun main() {
                    val a = kotlin.Int.MAX_VALUE
                    val b = kotlin.Int.Companion.MAX_VALUE
                    val c = kotlin.Int.Companion::MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("expression4") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                import kotlin.Int.Companion.MAX_VALUE
                
                fun main() {
                    val a = kotlin.Int.Companion.MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("expression5") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                import kotlin.Int.Companion.MAX_VALUE
                
                fun main() {
                    val a = kotlin.Int.MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("expressionWithParameter") {
            val code = """
                package my.simple.name
                
                fun run() {}
                fun go(check: () -> Unit) = check()
                
                fun main() {
                    val a = my.simple.name.go {
                        run()
                    }
                    val b = my.simple.name.go(::run)
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(2)
        }

        it("innerClass") {
            val code = """
                package my.simple.name
                
                class Outer {
                    class Middle {
                        class Inner {
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                
                fun main() {
                    my.simple.name.Outer.Middle.Inner.check()
                    Outer.Middle.Inner.check()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClass2") {
            val code = """
                package my.simple.name
                
                class Outer {
                    class Middle {
                        class Inner {
                            fun check() {
                                Outer.Middle.Inner.Companion.check()
                            }
                
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClass3") {
            val code = """
                package my.simple.name
                
                class Outer {
                    class Middle {
                        class Inner {
                            fun otherCheck() {
                                Outer.Middle.Inner.Companion.check()
                            }
                
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClass4") {
            val code = """
                package my.simple.name
                
                fun <T> check() {}
                class Outer {
                    class Middle {
                        class Inner {
                            fun foo() {
                                Middle.Inner.Companion.check()
                                my.simple.name.check<Outer>()
                            }
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClass5") {
            val code = """
                package my.simple.name
                
                fun <T> check() {}
                class Outer {
                    class Middle {
                        class Inner {
                            fun foo() {
                                Middle.Inner.Companion.check()
                                my.simple.name.check<Outer>()
                            }
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClassWithImport") {
            val code = """
                package my.simple.name
                
                import my.simple.name.Outer.Middle.Inner.Companion.check
                
                class Outer {
                    class Middle {
                        class Inner {
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                
                fun main() {
                    my.simple.name.Outer.Middle.Inner.check()
                    Outer.Middle.Inner.check()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("innerClassWithImport2") {
            val code = """
                package my.simple.name
                
                import my.simple.name.Outer.Middle.Inner.Companion.check
                
                class Outer {
                    class Middle {
                        class Inner {
                            companion object {
                                fun check() {}
                            }
                        }
                    }
                }
                
                fun main() {
                    my.simple.name.Outer.Middle.Inner.check()
                    Outer.Middle.Inner.check()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("javaConstructor") {
            val code = """
                // WITH_RUNTIME
                
                import java.util.ArrayList
                
                fun test() {
                    val a = java.util.ArrayList<Int>()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("localFun") {
            val code = """
                package my.simple.name
                
                class Inner {
                    fun a() {
                        fun Inner() {}
                        Inner.say()
                    }
                
                    companion object {
                        fun say() {}
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("localFun2") {
            val code = """
                package my.simple.name
                
                class Inner {
                    fun a() {
                        fun say(i: Inner) {}
                        Inner.say()
                    }
                
                    companion object {
                        fun say() {}
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("notApplicableAsReceiver") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                
                import C.G
                
                fun test() {
                    B.G.let { it }
                }
                
                class B {
                    object G
                }
                
                class C {
                    object G
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableAsReceiverProperty") {
            val code = """
                // PROBLEM: none
                
                package inspector.p30879
                import inspector.p30879.B.G
                
                val <T> T.letVar: Int; get() = 0
                
                fun test() {
                    C.G.letVar
                }
                
                class B { object G }
                class C { object G }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCollisionTopLevelClass") {
            val code = """
                // WITH_RUNTIME
                // PROBLEM: none
                package my.simple.name
                
                open class SuperClass {
                    companion object {
                        fun check() {}
                    }
                }
                
                class Foo
                
                class Child : SuperClass() {
                    class Foo constructor() {
                        constructor(i: Int) : this()
                        class SuperClass
                        fun check() {}
                
                        fun foo() {
                            my.simple.name.SuperClass.check()
                        }
                
                        companion object {
                            fun check() {}
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCompanion") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Child {
                    fun f() {
                        Companion.value
                    }
                
                    companion object {
                        val value = 1
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCompanionInEnumEntry") {
            val code = """
                // PROBLEM: none
                enum class C(val i: Int) {
                    ONE(C.K)
                    ;
                
                    companion object {
                        const val K = 1
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCompanionOtherName") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Child {
                    fun f() {
                        Helper.value
                    }
                
                    companion object Helper {
                        val value = 1
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCompanionType") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                import my.simple.name.Bar.Foo.Companion.CheckClass
                
                class Bar {
                    class Foo {
                        companion object {
                            class CheckClass
                        }
                    }
                }
                
                class F {
                    fun foo(a: Bar.Foo.Companion.CheckClass) {}
                
                    companion object {
                        class CheckClass
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableCompanionType2") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class F {
                    class CheckClass
                
                    fun foo(a: Companion.CheckClass) {}
                
                    companion object {
                        class CheckClass
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableEnumEntry") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                class Player {
                    val status: String = Encoding.MJPEG.toString()
                }
                
                enum class Encoding {
                    UNKNOWN,
                    MJPEG,
                    H264
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableEnumEntryInEnumEntry") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                fun main() {
                    MyEnum.B.foo()
                }
                
                enum class MyEnum {
                    A {
                        override fun foo() {
                            println("A")
                        }
                    },
                    B {
                        override fun foo() {
                            println("B")
                            A.foo()
                        }
                    };
                
                    abstract fun foo()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableEnumEntryInEnumEntry2") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                fun main() {
                    MyEnum.B.foo()
                }
                
                enum class MyEnum {
                    A {
                        override fun foo() {
                            println("A")
                        }
                    },
                    B {
                        override fun foo() {
                            println("B")
                            B.foo()
                        }
                    };
                
                    abstract fun foo()
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableEnumInEnum") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                enum class A
                
                enum class B() {
                    ;
                
                    fun test() {
                        A.values()
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableEnumInEnum2") {
            val code = """
                // PROBLEM: none
                // WITH_RUNTIME
                enum class A
                
                enum class B(val x: Int) {
                    BB(A.values().size)
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableExpression") {
            val code = """
                // WITH_RUNTIME
                // PROBLEM: none
                package my.simple.name
                
                fun main() {
                    val a = kotlin.Int.Companion.MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableExpression2") {
            val code = """
                // WITH_RUNTIME
                // PROBLEM: none
                package my.simple.name
                
                fun main() {
                    val a = kotlin.Int.Companion::MAX_VALUE
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableExpression3") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Foo {
                    val f = this
                    fun check() {
                        f.f.f.f.f.f.f.f.f
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableExpression4") {
            val code = """
                // PROBLEM: none
                package foo
                
                class Foo {
                    fun test() {
                        foo.myRun {
                            42
                        }
                    }
                }
                
                inline fun <R> myRun(block: () -> R): R = block()
                
                inline fun <T, R> T.myRun(block: T.() -> R): R = block()
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableInnerClassInGenericOuterClass") {
            val code = """
                // PROBLEM: none
                class Outer<T>(val inner: Outer<T>.Inner? = null) {
                    inner class Inner
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableInnerClassInGenericOuterClass2") {
            val code = """
                // PROBLEM: none
                interface Inv<X>
                
                class Outer<E> {
                    inner class Inner
                
                    class Nested : Inv<Outer<String>.Inner>
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableInnerClassInGenericOuterClass3") {
            val code = """
                // PROBLEM: none
                class Outer<E> {
                    inner class Inner
                
                    class Nested {
                        fun bar(x: Outer<String>.Inner) {}
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableInnerClassInGenericOuterClass4") {
            val code = """
                // PROBLEM: none
                class Outer<T>() {
                    inner class Inner
                
                    fun test(inner: Outer<T>.Inner?) {
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableLocalFun") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Inner {
                    fun a() {
                        fun say() {}
                        Inner.say()
                    }
                
                    companion object {
                        fun say() {}
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableLocalVariable") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Inner {
                    fun a() {
                        val MAX = 2
                        val a = Member.MAX
                    }
                
                    companion object Member {
                        val MAX = 1
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableLocalVariable2") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Inner {
                    fun a() {
                        val a = Member.MAX
                    }
                
                    companion object Member {
                        val MAX = 1
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableObject") {
            val code = """
                class Foo {
                    val prop = Obj.prop.toString()
                }
                
                object Obj {
                    val prop = "Hello"
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableObject2") {
            val code = """
                class A {
                    companion object {
                        val INST = A()
                    }
                }
                
                class B {
                    companion object {
                        val INST = B()
                    }
                
                    fun foo() {
                        A.INST.toString()
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableObject3") {
            val code = """
                open class A(init: A.() -> Unit) {
                    val prop: String = ""
                }
                
                object B : A({})
                
                object C : A({
                    fun foo() = B.prop.toString()
                })
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableOuterClass") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Foo {
                    companion object {
                        fun say(){}
                    }
                }
                
                class Bar {
                    class Inner {
                        fun a() {
                            my.simple.name.Foo.say()
                        }
                
                        class Foo
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableThis") {
            val code = """
                // PROBLEM: none
                package my.simple.name
                
                class Inner {
                    fun a() {
                        this.say()
                    }
                
                    fun say() {}
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableTypeWithRuntime") {
            val code = """
                // WITH_RUNTIME
                // PROBLEM: none
                package my.simple.name
                
                class Int
                
                fun foo(a: kotlin.Int) {
                
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("notApplicableTypeWithRuntime2") {
            val code = """
                // WITH_RUNTIME
                // PROBLEM: none
                package my.simple.name
                
                typealias Int = Long
                
                fun foo(a: kotlin.Int) {
                
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(0)
        }

        it("objectCallChain") {
            val code = """
                package my.ada.adad.ad
                
                import my.ada.adad.ad.Fixtures.Register.Domain.UserRepository
                
                object Fixtures {
                    object Register {
                        object Domain {
                            object UserRepository {
                                const val authSuccess = true
                            }
                        }
                    }
                }
                
                fun test() {
                    my.ada.adad.ad.Fixtures.Register.Domain.UserRepository.authSuccess
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("secondaryConstructor") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                class Outer {
                    class Foo constructor() {
                        constructor(i: Int) : this()
                
                        companion object {
                            fun check() {
                                val a = Outer.Foo(1)
                                val b = Outer.Foo()
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("superClass") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                open class SuperClass {
                    companion object {
                        fun check() {}
                    }
                }
                
                class Child : SuperClass() {
                    class Foo constructor() {
                        constructor(i: Int) : this()
                
                        fun foo() {
                            my.simple.name.SuperClass.check()
                            Child.Foo.check()
                        }
                
                        companion object {
                            fun check() {}
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("superClass2") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                open class SuperClass {
                    companion object {
                        fun check() {}
                    }
                }
                
                class Child : SuperClass() {
                    class Foo constructor() {
                        constructor(i: Int) : this()
                
                        fun foo() {
                            my.simple.name.SuperClass.check()
                            Child.Foo.check()
                        }
                
                        companion object {
                            fun check() {}
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("type") {
            val code = """
                package my.simple.name
                
                class Foo
                class Bar
                
                fun foo(a: my.simple.name.Foo) {
                    val b: my.simple.name.Bar
                    val c: my.simple.name.Foo
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(3)
        }

        it("type2") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                class Outer {
                    fun goo(i: Outer.Middle.Inner.Int) {
                
                    }
                    class Middle {
                        class Inner {
                            class Int
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("typeWithAlias") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                typealias Int = Long
                
                class Outer {
                    class Middle {
                        class Int
                        class Inner {
                            fun goo(i: Outer.Middle.Int) {
                
                            }
                        }
                    }
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("typeWithRuntime") {
            val code = """
                // WITH_RUNTIME
                package my.simple.name
                
                fun foo(a: kotlin.Int) {
                
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("userTypeWithTypeParameter") {
            val code = """
                // WITH_RUNTIME
                
                fun main() {
                    val a: kotlin.Pair<Int, Int>
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }

        it("withTypeParameter") {
            val code = """
                // WITH_RUNTIME
                
                fun main() {
                    kotlin.Pair<Int, Int>(1, 1)
                }
                """
            val lint = subject.compileAndLint(code)

            assertThat(lint).hasSize(1)
        }
    }
})
