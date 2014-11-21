#!/bin/bash
if [ ! -d "PLM-data" ]; then
  git clone https://github.com/mquinson/PLM-data.git
fi
cd PLM-data
i=0;
for branch in $(git branch -r); do
  if [[ "$branch" =~ ^origin/PLM.* ]]; then
    localBranch=`echo "$branch" | cut -d '/' -f 2`
    git checkout "$localBranch" -m
    for log in $(git log --pretty=format:"%h$%ad$%s" --date=iso | tr -d ' ' ); do
      i=$(($i + 1))
      echo "$log" # Log's format: commitHash$commitDate$commitMessage
    done
  fi
done
echo "$i commits" # Number of commits

