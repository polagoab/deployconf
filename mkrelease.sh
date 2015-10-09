#!/bin/sh
# The MIT License (MIT)
# Copyright (c) 2015 Polago AB
# All rights reserved.
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#
# Create a release of this project
#

project="deployconf"
cur_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -v 'Download'`

if [ $# -ne 1 ]; then
	echo "Usage: mkrelease.sh version"
	echo "Current $project version is $cur_version."
	exit 1
fi

branch=`git rev-parse --abbrev-ref HEAD`
if [ "master" != "$branch" ]; then
    echo "Not on branch master: $branch"
    exit 1
fi

set -e

version=$1
tag=v$version

echo "Creating a new release of $project with version: $version."
echo "Current $project version is $cur_version."
echo

#
# Update workspace from origin
#
echo "Updating local workspace from origin..."
git pull

echo "Verifying that everything is checked in..."
#
# git status --porcelain
#
stat=`git status --porcelain`

if [ ! -z "$stat" ]; then
    echo "The workspace is not clean"
    echo "$stat"
    exit 1
fi

#
# Make sure that all tests pass
#
echo "Running tests..."
mvn clean test

echo ""
read -p "Do you want to create the release[yn]" answer

if [ "$answer" != "y" ]; then
	echo "bailing out!"
	exit 1
fi

#
# Update version number in pom.xml
#
echo "Updating pom.xml with new version: $version"
mvn versions:set -DnewVersion=$version -DgenerateBackupPoms=false

#
# Commit pom.xml
#
echo "Committing pom.xml..."
git commit -m "$project version $version" pom.xml

#
# Creating tag in Git
#
echo "Creating annotated tag: $tag" 
git tag -m "$project version $version" -a $tag

#
# Publish to origin
#
echo "Pushing to origin..." 
git push

#
# Publishing Git tag to origin
#
echo "Publishing tag to origin: $tag"
git push origin $tag

#
# Deploying artifact to ossrh
#
mvn -Possrh clean deploy

#
# Publishing site
#
mvn site-deploy

echo "Please create a GitHub release info"
