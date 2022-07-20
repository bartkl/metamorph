<!--
SPDX-FileCopyrightText: 2022 Alliander N.V.

SPDX-License-Identifier: Apache-2.0
-->

# How to Contribute
This document describes what you can do to contribute to the project, how to go about it, and what is expected of you.

Please also refer to our [Code of Conduct](CODE_OF_CONDUCT.md).

## What you can do
There are several ways you can contribute to the project:

* **Development**
    * Implement or enhance features
    * Increase test coverage
    * Perform small refactors
* **Documentation**
    * Improve our documentation, particularly the Wiki and the documents in the project root
* **Engage**
    * Report bugs
    * Share your thoughts on discussions and issues
    * Submit requests for new features or enhancements
    * Help us promote the project and find new users and contributors
* **Transformation specification**
    * Help us map highly expressive semantic models to technical models

We have a way of working and guidelines one needs to conform to. These will be laid out shortly.

### Development
If you are interested in contributing as a developer, 

#### Quality guidelines



## Engage


You can file bugs against and change requests for the project via github issues. Consult [GitHub Help](https://docs.github.com/en/free-pro-team@latest/github/managing-your-work-on-github/creating-an-issue) for more
information on using github issues.


## 

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache 2.0 header:

    SPDX-FileCopyrightText: <YEAR> Alliander N.V.
    SPDX-License-Identifier: Apache-2.0

## Git branching

This project uses the [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) and branching model. The `master` branch always contains the latest release, after a release is made new feature branches are branched of `develop`. When a feature is finished it is merged back into `develop`. At the end of a sprint `develop` is merged back into `master` or (optional) into a `release` branch first before it is merged into `master`.

![Gitflow](img/gitflow.svg)

## Code reviews

All patches and contributions, including patches and contributions by project members, require review by one of the maintainers of the project. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

## Pull Request Process
Contributions should be submitted as Github pull requests. See [Creating a pull request](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request) if you're unfamiliar with this concept.

The process for a code change and pull request you should follow:

1. Create a topic branch in your local repository, following the naming format
"feature-[description]". For more information see the Git branching guideline.
1. Make changes, compile, and test thoroughly. Ensure any install or build dependencies are removed before the end of the layer when doing a build. Code style should match existing style and conventions, and changes should be focused on the topic the pull request will be addressed. For more information see the style guide.
1. Push commits to your fork.
1. Create a Github pull request from your topic branch.
1. Pull requests will be reviewed by one of the maintainers who may discuss, offer constructive feedback, request changes, or approve
the work. For more information see the Code review guideline.
1. Upon receiving the sign-off of one of the maintainers you may merge your changes, or if you
   do not have permission to do that, you may request a maintainer to merge it for you.


## Attribution

This Contributing.md is adapted from Google
available at
https://github.com/google/new-project/blob/master/docs/contributing.md
