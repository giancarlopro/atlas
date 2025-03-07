/*
 * Copyright 2014-2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.atlas.lwcapi

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.RouteTestTimeout
import com.netflix.atlas.core.util.SortedTagMap
import com.netflix.atlas.eval.model.LwcDiagnosticMessage
import com.netflix.atlas.lwcapi.EvaluateApi.*
import com.netflix.atlas.pekko.DiagnosticMessage
import com.netflix.atlas.pekko.testkit.MUnitRouteSuite
import com.netflix.spectator.api.NoopRegistry

class EvaluateApiSuite extends MUnitRouteSuite {

  import scala.concurrent.duration.*

  private implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.second)

  private val sm = new StreamSubscriptionManager(new NoopRegistry)
  private val endpoint = new EvaluateApi(new NoopRegistry, sm)

  test("post empty payload") {
    val json = EvaluateRequest(1234L, Nil, Nil).toJson
    Post("/lwc/api/v1/evaluate", json) ~> endpoint.routes ~> check {
      assertEquals(response.status, StatusCodes.OK)
    }
  }

  test("post metrics") {
    val metrics = List(Item("abc", SortedTagMap("a" -> "1"), 42.0))
    val json = EvaluateRequest(1234L, metrics, Nil).toJson
    Post("/lwc/api/v1/evaluate", json) ~> endpoint.routes ~> check {
      assertEquals(response.status, StatusCodes.OK)
    }
  }

  test("post diagnostic message") {
    val msgs = List(LwcDiagnosticMessage("abc", DiagnosticMessage.error("bad expression")))
    val json = EvaluateRequest(1234L, Nil, msgs).toJson
    Post("/lwc/api/v1/evaluate", json) ~> endpoint.routes ~> check {
      assertEquals(response.status, StatusCodes.OK)
    }
  }

  test("post missing messages field") {
    val json = """{"timestamp":12345,"metrics":[]}"""
    Post("/lwc/api/v1/evaluate", json) ~> endpoint.routes ~> check {
      assertEquals(response.status, StatusCodes.OK)
    }
  }

  test("post missing metrics field") {
    val json = """{"timestamp":12345,"messages":[]}"""
    Post("/lwc/api/v1/evaluate", json) ~> endpoint.routes ~> check {
      assertEquals(response.status, StatusCodes.OK)
    }
  }
}
