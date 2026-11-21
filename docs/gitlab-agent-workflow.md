# GitLab AI Agent Workflow Reference

This guide documents the GitLab integration commands and natural-language prompt mapping available in Antigravity IDE using the GitLab CLI (`glab`) and GitLab Workflow extension.

---

## 1. Natural Language Prompt Mapping

| User Intent / Natural Language Prompt | Underlying Command / API Action |
| :--- | :--- |
| *"Inspect GitLab repository status"* | `glab repo view <namespace/repo>` |
| *"List branches and recent commits"* | `glab repo view <repo>` & `git log` / `glab api projects/:id/repository/branches` |
| *"Create a feature branch"* | `git checkout -b <branch>` or `glab api projects/:id/repository/branches --field branch=<name> --field ref=master` |
| *"Review repository changes"* | `glab mr diff <mr-id> -R <repo>` or `git diff` |
| *"Commit approved changes"* | `git commit -m "<message>"` or GitLab Commits API (`POST /projects/:id/repository/commits`) |
| *"Push branches"* | `git push <remote> <branch>` (authenticated via `glab auth git-credential`) |
| *"Create a merge request"* | `glab mr create -R <target-repo> --source-branch <branch> --target-branch master --title "<title>" --description "<desc>" --yes` |
| *"Inspect merge requests"* | `glab mr list -R <repo>` / `glab mr view <mr-id> -R <repo>` |
| *"Review merge request discussions"* | `glab mr view <mr-id> -R <repo> --comments` or `glab api projects/:id/merge_requests/:mr_id/notes` |
| *"Reply to merge request comments"* | `glab mr note -m "<reply>" <mr-id> -R <repo>` |
| *"Monitor CI pipelines"* | `glab ci list -R <repo>` or `glab ci status -R <repo>` |
| *"Inspect failed pipeline logs"* | `glab ci trace <job-id> -R <repo>` |

---

## 2. Active GitLab Configuration

- **CLI Tool:** `glab` (v1.131.0)
- **Host:** `gitlab.com`
- **Authenticated Account:** `Dr-SuBZeRo`
- **Git Protocol:** `https` (configured with `!glab auth git-credential`)
- **Credential Storage:** Windows Credential Manager (Keyring)
- **IDE Extension:** `gitlab.gitlab-workflow` (v7.7.1)
- **Active Submission MR:** [fdroid/fdroiddata!58487](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/58487)
