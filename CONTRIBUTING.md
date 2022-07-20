<!--
SPDX-FileCopyrightText: 2022 Alliander N.V.

SPDX-License-Identifier: Apache-2.0
-->

# Contributing
This document describes what you can do to contribute to the project, how to go about it, and what is expected of you.

Please also refer to our [Code of Conduct](CODE_OF_CONDUCT.md).

*Note*: the project is currently at an early stage, so it's generally advisable to [contact us](SUPPORT.md) before you contribute.


## Things you could do
There are several ways you can contribute to the project. To give you a quick idea:

* **Development**
    * Implement or enhance features.
    * Increase test coverage.
    * Perform small refactors.
* **Transformation specification**
    * Help us map highly expressive semantic models onto technical models.
* **Documentation**
    * Improve the Wiki and project documents.
* **Engage**
    * Report bugs.
    * Submit requests for new features or enhancements.
    * Share your thoughts on discussions and issues.
    * Help us promote the project and find new users and contributors.

## How to contribute
First, make sure you're familiar with our [Way of Working](wiki), most notably how we use GitHub's *Issues*, *Discussions* and *Projects*.

The follow steps will then guide you through the process.

1. **Check out the currently existing issues and discussions.**<br />See []
    * 
2. **Make sure what you (plan to) do is described in an issue or discussion.**<br />If none exists yet, create one.<br />
    * We maintain an overview of our work inventory and planning.
    * It will encourage you to think well about what it is you wish to contribute, and enable others to refine and discuss this.
    * If for some reason you wish to deviate from this way of working, you are free to do so *at own risk* of rejection.

2. 


<!-- Furthermore, we have a way of working and guidelines one needs to conform to. These will be explained below as it becomes relevant. -->








## How to contribute
## Places to Look
The following places give insight in what work is currently inventorized and planned.

* [**Issues**](https://github.com/alliander-opensource/schema-transformer/issues)
    * This is where all issues are maintained, including where new ones should be submitted.
* [**Kanban board**](https://github.com/orgs/alliander-opensource/projects/3)
    * Provides a view of planned issues, their progress and assignees.
* [**Discussions**](https://github.com/alliander-opensource/schema-transformer/discussions)
    * Here discussions take place.

Go about it as follows:

1. **Make sure what you plan to do is described in an issue.** If no issue or discussion exists yet, create one.
    * 

This way we can maintain an overview of our work inventory and planning. Furthermore it will encourage you to think well about what it is you wish to contribute, and enable others to refine and discuss this. The last thing we want is for you to waste your time.  If for some reason, however, you do wish to deviate from this way of working, you are free to do so *at own risk*.
    * If no issue exists yet, create one.
    * 

To contribute, make sure there is an issue or discussion to which the contribution applies, or create one in case none exists. This way we can maintain an overview of our work inventory and planning. Furthermore it will encourage you to think well about what it is you wish to contribute, and enable others to refine and discuss this. The last thing we want is for you to waste your time.  If for some reason, however, you do wish to deviate from this way of working, you are free to do so *at own risk*.




Furthermore, we have a way of working and guidelines one needs to conform to. These will be explained below as it becomes relevant.

*Note*: the project is currently at an early stage, so it's generally advisable to [contact us](SUPPORT.md) before you contribute.




### Development
When the project reaches a higher level of maturity, we will update this section.
If you like to contribute as a developer, please [contact us](SUPPORT.md).


For more information, refer to the wiki at [Development].
<!-- - copyright/license metadata
- linting set-up
- git workflow (don't refer to Git flow unless we know exactly where we do and don't adhere to it)
- code review requirements -->

## Engage
There are plenty of non-technical ways in which you can contribute. You may want to look at these places to get started:

* https://github.com/alliander-opensource/schema-transformer/issues
* Discussions
* The [Support](SUPPORT.md) document





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
