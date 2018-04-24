package com.prisma.deploy.schema.queries

import com.prisma.auth.{AuthImpl, AuthSuccess}
import com.prisma.deploy.specutils.DeploySpecBase
import com.prisma.shared.models.{Project, ProjectId}
import org.scalatest.{FlatSpec, Matchers}

trait GenerateProjectTokenSpec extends FlatSpec with Matchers with DeploySpecBase {
  val auth = AuthImpl

  "the GenerateProjectToken query" should "return a proper token for the requested project" in {
    val (project: Project, _)  = setupProject(schema = basicTypesGql, secrets = Vector("super-duper-secret"))
    val ProjectId(name, stage) = ProjectId.fromEncodedString(project.id)
    val result                 = server.query(s"""
                                       |query {
                                       |  generateProjectToken(name: "$name", stage: "$stage")
                                       |}
      """.stripMargin)

    val token = result.pathAsString("data.generateProjectToken")
    auth.verify(project.secrets, Some(token)) should be(AuthSuccess)
  }

  "the GenerateProjectToken query" should "return an empty string if the requested project does not have any secrets" in {
    val (project, _)           = setupProject(basicTypesGql)
    val ProjectId(name, stage) = ProjectId.fromEncodedString(project.id)
    val result                 = server.query(s"""
                                                 |query {
                                                 |  generateProjectToken(name: "$name", stage: "$stage")
                                                 |}
      """.stripMargin)

    val token = result.pathAsString("data.generateProjectToken")
    token should equal("")
    auth.verify(project.secrets, Some(token)) should be(AuthSuccess)
  }
}
