<img src="https://wcm.io/images/favicon-16@2x.png"/> AEM Mocks
======
[![Build](https://github.com/wcm-io/io.wcm.testing.aem-mock/actions/workflows/maven-build.yml/badge.svg?branch=develop)](https://github.com/wcm-io/io.wcm.testing.aem-mock/actions?query=workflow%3ABuild+branch%3Adevelop)
[![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.testing.aem-mock)](https://repo1.maven.org/maven2/io/wcm/io.wcm.testing.aem-mock/)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=wcm-io_io.wcm.testing.aem-mock&metric=coverage)](https://sonarcloud.io/summary/new_code?id=wcm-io_io.wcm.testing.aem-mock)

Mock implementation of selected AEM APIs.

Documentation: https://wcm.io/testing/aem-mock/<br/>
Issues: https://github.com/wcm-io/io.wcm.testing.aem-mock/issues<br/>
Wiki: https://wcm-io.atlassian.net/wiki/<br/>
Continuous Integration: https://github.com/wcm-io/io.wcm.testing.aem-mock/actions<br/>
Commercial support: https://wcm.io/commercial-support.html


## Build from sources

If you want to build wcm.io from sources make sure you have configured all [Maven Repositories](https://wcm.io/maven.html) in your settings.xml.

See [Maven Settings](https://github.com/wcm-io/io.wcm.testing.aem-mock/blob/develop/.maven-settings.xml) for an example with a full configuration.

Then you can build using

```
mvn clean install
```

## Refresh testing branches

Manually run the [Rebase testing branches](.github/workflows/rebase-testing-branches.yml)
workflow in GitHub Actions to rebase all `testing/` branches onto the latest `develop`
commit. Branches already containing that commit are skipped. Each successfully
pushed rebase explicitly triggers the Build workflow using `GITHUB_TOKEN`; no
personal access token is required.

Rebase conflicts and rejected pushes are reported as workflow failures without
overwriting the affected remote branch, and processing continues for other branches.
Branch protection rules must allow the workflow to force-push to `testing/` branches.
If build dispatch fails after a successful push, manually run the Build workflow
for that branch; rerunning the rebase workflow will skip it.
