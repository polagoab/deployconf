# Deployconf [![Build Status](https://github.com/polagoab/deployconf/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/polagoab/deployconf/actions/workflows/build.yml)

This repository contains the source code for [deployconf](http://www.polago.org/deployconf), a Command Line Tool
used by Deployers to configure java artifacts for the actual deployment environment.

## Dependency updates

Dependabot opens pull requests for Maven dependencies and GitHub Actions. All of them get the `dependency-update`
label. The `PR Cooldown Label` workflow adds `cooldown-passed` to them 3 days after their latest commit, as a signal
that they are ready to merge. When a pull request is updated, `cooldown-passed` is replaced by `cooldown-updated`
until the new cooldown has passed. The Dependabot cooldown is therefore disabled, so its pull requests are opened as
soon as a release appears.
