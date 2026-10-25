# egovframe-simple-homepage-template: project context

## Role and maintenance boundary

A reference Spring/JSP simple-homepage template with main pages, user, notice, board, guide, and schedule functionality. Preserve existing JSP routes, administrator semantics, the documented /sht_webapp context, and database mapper contracts.
This card reflects source inspection at adoption; it does not certify runtime behavior.

## Public entry points and evidence

- `README.md`: reference-project scope, administrator-only premise, mandatory `/sht_webapp` context, sample-only menu pages, database scripts, and Eclipse Run on Server setup.
- `pom.xml`: WAR packaging, Java 17, eGovFrame web-config parent, Selenium and Surefire selecting `**/*Test.java`.
- `.github/workflows/maven.yml`: JDK 17 and `mvn -B package --file pom.xml -Dmaven.test.skip=true`.
- `src/test/`: service, mapper/controller and utility regressions; `EgovLoginControllerTestSelenium.java` does not match the configured suffix selection.

## Build and verification entry points

CI uses `mvn -B package --file pom.xml -Dmaven.test.skip=true`, which skips test compilation
and execution. A future focused check can use `mvn -Dtest=ClassName test` after inspecting
the selected test and required dependencies; `mvn test` alone is not evidence that the
Selenium login scenario ran. README runtime validation requires configured DB schema/data
and Eclipse Run on Server with the documented context. No Selenium discovery fix is included
in adoption (fork issue #2 remains a separate task).

## Environment and evidence limits

README declares JDK 17, Jakarta EE 9, Servlet 5.0 and Spring Framework 6.2.11. Browser,
server, database and fixture setup constrain integration checks. Preserve the distinction
between functional pages and static sample menus; do not infer production authorization
policy from a demonstration login.
No application build, test suite, formatter, device test, or external service was run for this
workflow-only adoption. A Spec Kit CLI/status and isolated scaffold smoke validate the tooling
only. No CodeGraph index was present in the adoption workspace; targeted file listing and
exact text inspection supplied the evidence, without installing an indexer.

## Upstream and submission baseline

Upstream: [eGovFramework/egovframe-simple-homepage-template](https://github.com/eGovFramework/egovframe-simple-homepage-template). Fetch the actual upstream default branch
and record its exact SHA before preparing `upstream-submit`; do not infer the baseline from
fork work. Preserve an existing submission branch or open PR. Adoption changes are pushed
only to fork `work`; an absent `upstream-submit` is created from the fetched upstream SHA
without adoption commits, as a separate submission baseline.
The [constitution](constitution.md) is the policy source for domain cards, verification, and
work-only artifact isolation. Native OMP commands are in `.omp/commands/`; the bundled bug
workflow provides assess, fix, and test stages. No application fix or PR is part of
this adoption.
