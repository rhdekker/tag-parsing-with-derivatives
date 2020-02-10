package nl.knaw.huc.di.rd.parsec

import lambdada.parsec.io.Reader
import lambdada.parsec.parser.Parser
import lambdada.parsec.utils.Location
import org.eclipse.lsp4j.Position
import java.net.URL
import lambdada.parsec.parser.`try` as tryp

// `or`  with `try`
infix fun <I, A> Parser<I, A>.ort(p: Parser<I, A>): Parser<I, A> = { reader ->
    val a = tryp(this)(reader)
    when (a.consumed) {
        true -> a
        false -> a.fold({ a }, { tryp(p)(reader) })
    }
}

class PositionalReader(private val source: List<Char>,
                       private val location: Int,
                       val lastPosition: Position,
                       private val newline: Boolean) : Reader<Char> {

    override fun location(): Location {
        return Location(location)
    }

    override fun read(): Pair<Char, PositionalReader>? {
        return source.getOrNull(location)?.let {
            val character = if (newline) 0 else lastPosition.character + 1
            val line = if (newline) lastPosition.line + 1 else lastPosition.line
            it to PositionalReader(source, location + 1, Position(line, character), (it == '\n'))
        }
    }

    companion object {
        fun string(s: String): PositionalReader = PositionalReader(s.toList(), 0, Position(0, -1), false)
        fun url(s: URL): PositionalReader = string(s.readText())
    }

}
