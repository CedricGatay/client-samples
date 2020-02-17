package hellofx

import org.scalatest.FunSuite

class InitEclairTest  extends FunSuite {

  test("Test JSON parsing") {
    val maybeMessage = InitEclair.parseMessage(
      """
        |{
        |   "switchServer": {
        |     "uri": "http://toto.com"
        |   }
        |}
        |""".stripMargin)
  assert(maybeMessage.isDefined)
    assert(maybeMessage.get.equals(EclairMessage(Some(SwitchServer("http://toto.com")))))
}

  test("Test JSON parsing inline") {
    val maybeMessage = InitEclair.parseMessage(
      """{"switchServer":{"uri":"03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134@34.250.234.192:9735"}}""".stripMargin)
  assert(maybeMessage.isDefined)
    assert(maybeMessage.get.equals(EclairMessage(Some(SwitchServer("03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134@34.250.234.192:9735")))))
}

}
