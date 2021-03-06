/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package algolia.definitions

import algolia.http.{GET, HttpPayload, POST}
import algolia.inputs.{Request, Requests}
import algolia.objects.RequestOptions
import org.json4s.Formats
import org.json4s.native.Serialization._

case class GetObjectDefinition(
    index: Option[String] = None,
    oid: Option[String] = None,
    attributesToRetrieve: Iterable[String] = Iterable.empty,
    requestOptions: Option[RequestOptions] = None)(implicit val formats: Formats)
    extends Definition {

  type T = GetObjectDefinition

  def objectIds(oids: Seq[String]): GetObjectsDefinition =
    GetObjectsDefinition(index, oids)

  def from(ind: String): GetObjectDefinition = copy(index = Some(ind))

  def objectId(objectId: String): GetObjectDefinition =
    copy(oid = Some(objectId))

  def attributesToRetrieve(attributesToRetrieve: Iterable[String]): GetObjectDefinition =
    copy(attributesToRetrieve = attributesToRetrieve)

  override def options(requestOptions: RequestOptions): GetObjectDefinition =
    copy(requestOptions = Some(requestOptions))

  override private[algolia] def build(): HttpPayload = {
    val parameters = if (attributesToRetrieve.isEmpty) {
      None
    } else {
      Some(
        Map(
          "attributesToRetrieve" -> attributesToRetrieve.mkString(",")
        ))
    }

    HttpPayload(
      GET,
      Seq("1", "indexes") ++ index ++ oid,
      queryParameters = parameters,
      isSearch = true,
      requestOptions = requestOptions
    )
  }
}

case class GetObjectsDefinition(
    index: Option[String],
    oids: Seq[String] = Seq(),
    requestOptions: Option[RequestOptions] = None)(implicit val formats: Formats)
    extends Definition {

  type T = GetObjectsDefinition

  override def options(requestOptions: RequestOptions): GetObjectsDefinition =
    copy(requestOptions = Some(requestOptions))

  override private[algolia] def build(): HttpPayload = {
    val requests = oids.map { oid =>
      Request(index, oid)
    }

    HttpPayload(
      POST,
      Seq("1", "indexes", "*", "objects"),
      body = Some(write(Requests(requests))),
      isSearch = true,
      requestOptions = requestOptions
    )
  }

}
