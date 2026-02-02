@echo off
REM Step 1: Create a new branch with NO history
git checkout --orphan fresh_start

REM Step 2: Remove everything from the index (files stay on disk)
git rm -rf --cached .

REM Step 3: Add everything back (this time .gitignore will work)
git add .

REM Step 4: Create a single clean commit
git commit -m "Deployment preparation: cloud config, Dockerfile, and frontend fixes (Clean Start)"

REM Step 5: Delete the local main branch
git branch -D main

REM Step 6: Rename our fresh branch to main
git branch -m main

echo "Local cleanup finished. Ready to push."
