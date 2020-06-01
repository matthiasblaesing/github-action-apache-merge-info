Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements; and to You under the Apache License, Version 2.0.

Show detailed commit info for Apache projects on GitHub
=======================================================

In the commit view of the GitHub UI the author information recorded in the
commit is not visible. The only information accessible is the information GitHub
perceives as relevant and this GitHub replaces the author info with the
information from the GitHub database.

This action adds a comment to each PR, that shows:

- the public information about the PR requestor
- the author information of each commit as recorded in the commit data
- for each found author the information whether an ICLA could be found
- hints about which special concerns should be applied when considering merging

This action offers the committer more information, without taking away control.

How to use
----------

This project is designed to be used with custom workflows:

https://help.github.com/en/actions/configuring-and-managing-workflows/configuring-a-workflow

A sample can be found in the .github/workflows/main.yml file. GitHub by default
places all relevant information in the environment of the running action. The
only information, that needs to be explicitly passed is the GITHUB_TOKEN. The
token is needed to be able to create the info comment on the PR.

Democontent
-----------

Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod
tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At
vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren,
no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit
amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
sanctus est Lorem ipsum dolor sit amet.