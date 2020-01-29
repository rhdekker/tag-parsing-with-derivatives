package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.All
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.ConcurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NOT_ALLOWED
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.OneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Text

object Constructors {

    private fun notAllowed(): Pattern {
        return NOT_ALLOWED
    }

    fun empty(): Pattern {
        return EMPTY
    }

    fun text(): Pattern {
        return TEXT
    }

    fun anyContent(): Pattern = text() // might not cover it

    internal fun mixed(pattern: Pattern): Pattern {
        return interleave(text(), pattern)
    }

    fun zeroOrMore(pattern: Pattern): Pattern {
        return choice(oneOrMore(pattern), empty())
    }

    fun after(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return notAllowed()

        if (pattern1 is Empty) return pattern2

        if (pattern1 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            return after(p1, after(p2, pattern2))
        }

        return After(pattern1, pattern2)
    }

    fun all(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return notAllowed()

        if (pattern2 is Empty) {
            return if (pattern1.nullable)
                empty()
            else
                notAllowed()
        }

        if (pattern1 is Empty) {
            return if (pattern2.nullable)
                empty()
            else
                notAllowed()
        }

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            val e3 = pattern2.pattern1
            val e4 = pattern2.pattern2
            return after(all(e1, e3), all(e2, e4))
        }

        return All(pattern1, pattern2)
    }

    fun choice(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 == pattern2) return pattern1
        if (pattern1 is NotAllowed) return pattern2
        if (pattern2 is NotAllowed) return pattern1
        return Choice(pattern1, pattern2)
    }

    private fun oneOrMore(pattern: Pattern): Pattern {
        return if (pattern is NotAllowed
                || pattern is Empty)
            pattern
        else
            OneOrMore(pattern)
    }

    fun concurOneOrMore(pattern: Pattern): Pattern {
        return if (pattern is NotAllowed
                || pattern is Empty)
            pattern
        else
            ConcurOneOrMore(pattern)
    }

    fun concur(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return notAllowed()
        if (pattern1 is Text) return pattern2
        if (pattern2 is Text) return pattern1

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            val e3 = pattern2.pattern1
            val e4 = pattern2.pattern2
            return after(all(e1, e3), concur(e2, e4))
        }

        if (pattern1 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            return after(e1, concur(e2, pattern2))
        }

        if (pattern2 is After) {
            val e2 = pattern2.pattern1
            val e3 = pattern2.pattern2
            return after(e2, concur(pattern1, e3))
        }

        return Concur(pattern1, pattern2)
    }

    fun group(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  group p NotAllowed = NotAllowed
        //  group NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return notAllowed()

        //  group p Empty = p
        if (pattern2 is Empty) return pattern1

        //  group Empty p = p
        if (pattern1 is Empty) return pattern2

        //  group (After p1 p2) p3 = after p1 (group p2 p3)
        if (pattern1 is After) return after(pattern1.pattern1, group(pattern1.pattern2, pattern2))

        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, group(pattern1, pattern2.pattern2))
        } else Group(pattern1, pattern2)
        //  group p1 p2 = Group p1 p2
    }

    fun interleave(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  interleave p NotAllowed = NotAllowed
        //  interleave NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return notAllowed()

        //  interleave p Empty = p
        if (pattern2 is Empty) return pattern1

        //  interleave Empty p = p
        if (pattern1 is Empty) return pattern2

        //  interleave (After p1 p2) p3 = after p1 (interleave p2 p3)
        if (pattern1 is After) return after(pattern1.pattern1, interleave(pattern1.pattern2, pattern2))

        //  interleave p1 (After p2 p3) = after p2 (interleave p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, interleave(pattern1, pattern2.pattern2))
        } else Interleave(pattern1, pattern2)
        //  interleave p1 p2 = Interleave p1 p2
    }
}
