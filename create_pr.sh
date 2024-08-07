#!/usr/bin/env bash

git checkout master
git checkout -b update_comment
date=$(date)
sed "11s/.*/\/\/Updated comment on $date/" build.gradle > build.gradle
git add build.gradle
git commit -m "Update build.gradle"
git push origin update_comment
pr_url=$(gh pr create --base master --title "Update build.gradle $date" --body "Update build.gradle")
gh merge $pr_url -d
