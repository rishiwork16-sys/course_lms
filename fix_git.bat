@echo off
git reset --soft HEAD~2
git rm -r --cached .
git add .
git commit -m "Deployment preparation: cloud config, Dockerfile, and frontend fixes"
git status
