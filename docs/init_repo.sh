#!/bin/bash
# Initialize GitHub repo for Android TV IPTV app

# Variables (replace with my values before running)
GITHUB_USER="nunezshow"
REPO_NAME="nuneTV"

# Create repo on GitHub via API
curl -u $GITHUB_USER \
  https://api.github.com/user/repos \
  -d "{\"name\":\"$REPO_NAME\",\"private\":false}"

# Init Git locally
git init
git branch -M main
git remote add origin https://github.com/$GITHUB_USER/$REPO_NAME.git

# First commit & push
git add .
git commit -m "chore: initialize Android TV IPTV project structure"
git push -u origin main
