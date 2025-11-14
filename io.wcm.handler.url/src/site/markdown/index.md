## About URL Handler

URL resolving and processing.

[![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.handler.url)](https://repo1.maven.org/maven2/io/wcm/io.wcm.handler.url/)


### Documentation

* [Usage][usage]
* [General concepts][general-concepts]
* [Suffix Builder and Parser][suffix-builder-parser]
* [Integrator Template Mode][integrator]
* [Sling Rewriter Integration][rewriter]
* [Granite UI components][graniteui-components]
* [System configuration][configuration]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The URL Handler provides:

* Building URLs from path, selectors, extension, suffix query string an fragment parts
* Externalizing links for page links and frontend resources
* Supporting different URL Modes for externalizing to HTTP/HTTPs, with full hostname or protocol-relative mode
* Hostnames used for externalization for HTTP and HTTPs are stored in [Context-Aware Configuration][caconfig]
* Rewrites URLs to current site
* [Suffix Builder and Parser][suffix-builder-parser] for passing around information via Sling Suffix string
* Supports externalizing URLs for [Integrator Template Mode][integrator] with placeholders or Full URLs
* Supports externalizing URLs in generated markup via [Sling Rewriter][rewriter]
* Rewrites resource URLs pointing to client libraries with "allowProxy" mode to `/etc.clientlibs`.
* Generic Sling Models for usage in views: [Sling Models][ui-package]
* Generic [Granite UI components][graniteui-components] that can be used in link dialogs

Read the [general concepts][general-concepts] to get an overview of the functionality.


### AEM Version Support Matrix

|URL Handler version |AEM version supported
|--------------------|----------------------
|2.0.x or higher     |AEM 6.5.17+, AEMaaCS
|1.7.2 or higher     |AEM 6.5.7+, AEMaaCS
|1.7.0               |AEM 6.5+, AEMaaCS
|1.5.x - 1.6.x       |AEM 6.4.5+, AEMaaCS
|1.4.x               |AEM 6.3.3+, AEM 6.4.5+
|1.2.x - 1.3.x       |AEM 6.2+
|1.0.x - 1.1.x       |AEM 6.1+
|0.x                 |AEM 6.0+


### Dependencies

To use this module you have to deploy also:

|---|---|---|
| [wcm.io Sling Commons](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.commons/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.sling.commons)](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.commons/) |
| [wcm.io AEM Sling Models Extensions](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.models/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.sling.models)](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.models/) |
| [wcm.io WCM Commons](https://repo1.maven.org/maven2/io/wcm/io.wcm.wcm.commons/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.wcm.commons)](https://repo1.maven.org/maven2/io/wcm/io.wcm.wcm.commons/) |
| [wcm.io WCM Granite UI Extensions](https://repo1.maven.org/maven2/io/wcm/io.wcm.wcm.ui.granite/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.wcm.ui.granite)](https://repo1.maven.org/maven2/io/wcm/io.wcm.wcm.ui.granite/) |
| [wcm.io Handler Commons](https://repo1.maven.org/maven2/io/wcm/io.wcm.handler.commons/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.handler.commons)](https://repo1.maven.org/maven2/io/wcm/io.wcm.handler.commons/) |


### GitHub Repository

Sources: https://github.com/wcm-io/io.wcm.handler.url


[usage]: usage.html
[general-concepts]: general-concepts.html
[suffix-builder-parser]: suffix-builder-parser.html
[integrator]: integrator.html
[rewriter]: rewriter.html
[ui-package]: apidocs/io/wcm/handler/url/ui/package-summary.html
[graniteui-components]: graniteui-components.html
[configuration]: configuration.html
[apidocs]: apidocs/
[changelog]: changes.html
[caconfig]: ../../caconfig/
