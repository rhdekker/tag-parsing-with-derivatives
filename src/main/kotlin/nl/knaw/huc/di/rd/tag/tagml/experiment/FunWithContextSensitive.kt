package nl.knaw.huc.di.rd.tag.tagml.experiment;

import arrow.core.andThen
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import nl.knaw.huc.di.rd.parsec.PositionalReader
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.endTag
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.startTag
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.text


fun main() {
 // ultra simple parsec example
 val foo: Parser<Char, List<Char>> = not(char(',')).rep
 val input = Reader.string("hello, parsec!")
 val result = foo(input)
 when (result) {
  is Response.Accept -> println("good")
  is Response.Reject -> println("bad")
 }

 // we want to have a grammar rule that the each opened TAGML tag has a corresponding close tag.
 // Unlike XML we don't want to use a stack
 // instead we dynamically generated rules, by using a flatmap function.
val foo2: Parser<Char, LSPToken> = startTag.flatMap { lspToken ->  endTag }
 val input2 = PositionalReader.string("[tagml><tagml]")
// val input2 = PositionalReader.string("[tagml>test<tagml]")
//(text andThen)
 val result2 = foo2(input2)
 println(result2)
}