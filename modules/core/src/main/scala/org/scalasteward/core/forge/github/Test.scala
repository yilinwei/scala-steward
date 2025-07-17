package org.scalasteward.core.forge.github

import org.scalasteward.core.data.Repo
// import org.scalasteward.core.forge.data.PullRequestState

import cats.syntax.all.*
import cats.effect.*
import org.http4s.syntax.all.*
import org.http4s.jdkhttpclient.JdkHttpClient
import org.http4s.client.Client
import java.net.http.HttpClient
import org.scalasteward.core.util.HttpJsonClient
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.http4s.*
import org.scalasteward.core.forge.data.NewPullRequestData

object GithubApiTest extends IOApp {

  implicit private def client: Client[IO] = JdkHttpClient(HttpClient.newHttpClient())
  implicit private def httpJsonClient: HttpJsonClient[IO] = new HttpJsonClient[IO]
  implicit private def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def creds(token: String) = Credentials.Token(AuthScheme.Bearer, token)

  private def auth(token: String)(req: Request[IO]): IO[Request[IO]] =
    req.putHeaders(headers.Authorization(creds(token))).pure[IO]

  def run(args: List[String]): IO[ExitCode] = {
    val api = new GitHubApiAlg[IO](uri"https://api.github.com", auth(args.head))
    val repo = Repo("coralogix", "eng-svc-dataprime-api")
    (for {
      repoOut <- api.getRepo(repo)
      // prs <- api.listPullRequests(repo, repoOut.default_branch.name, repoOut.default_branch)
      // pr =
      //   prs
      //     .filter(_.state == PullRequestState.Closed)
      //     .filter(_.title.startsWith("Update"))
      //     .head
      // _ <- api.labelPullRequest(
      //   repo,
      //   pr.number,
      //   List("dummy")
      // )
      pr <- api.createPullRequest(
        repo,
        NewPullRequestData(
          "dummy PR",
          "some body",
          repoOut.default_branch.name,
          repoOut.default_branch,
          labels = List("dummy"),
          List.empty,
          List.empty
        )
      )
    } yield println(pr)).as(ExitCode.Success)
  }
}
