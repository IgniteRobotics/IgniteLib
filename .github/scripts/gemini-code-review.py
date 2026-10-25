#!/usr/bin/env python3
"""
AI Code Review Script using Gemini API
Posts friendly, constructive code reviews on GitHub pull requests
"""

import os
import sys
from pathlib import Path

from github import Auth, Github, GithubException
from google import genai
from google.genai import types

# Configuration
GEMINI_MODEL = os.environ.get("GEMINI_MODEL") or "gemini-2.5-flash"
MAX_FILES_TO_REVIEW = 50
MAX_FILE_SIZE = 100000  # 100KB per file
MAX_FILE_CHANGES = 1000
REVIEW_COMMENT_MARKER = "<!-- ai-code-review-bot -->"
BOT_LOGIN = "github-actions[bot]"

FENCE_LANGUAGES = {
    ".java": "java",
    ".gradle": "groovy",
    ".kts": "kotlin",
    ".yml": "yaml",
    ".yaml": "yaml",
    ".py": "python",
    ".json": "json",
    ".md": "markdown",
    ".sh": "bash",
    ".xml": "xml",
    ".properties": "properties",
}


def fence_language(filename: str) -> str:
    """Pick the code-fence language for a file based on its extension"""
    return FENCE_LANGUAGES.get(Path(filename).suffix.lower(), "")


def setup_gemini(api_key: str) -> genai.Client:
    """Initialize Gemini API client"""
    return genai.Client(api_key=api_key)


def load_prompt_template() -> str:
    """Load the code review prompt from markdown file"""
    prompt_path = Path(__file__).parent.parent / "prompts" / "code-review-prompt.md"
    try:
        return prompt_path.read_text(encoding="utf-8")
    except FileNotFoundError:
        print(f"Warning: Prompt file not found at {prompt_path}")
        return "Please review this code and provide constructive feedback."


def get_changed_files(repo, pr_number: int):
    """Fetch changed files from the pull request"""
    pr = repo.get_pull(pr_number)
    changed_files = []

    for file in pr.get_files():
        # Skip deleted files and very large files
        if file.status == "removed":
            continue
        if file.changes > MAX_FILE_CHANGES:
            print(f"Skipping large file: {file.filename} ({file.changes} changes)")
            continue

        try:
            content = repo.get_contents(file.filename, ref=pr.head.sha)
            if content.size > MAX_FILE_SIZE:
                print(f"Skipping large file: {file.filename} ({content.size} bytes)")
                continue

            changed_files.append(
                {
                    "filename": file.filename,
                    "status": file.status,
                    "additions": file.additions,
                    "deletions": file.deletions,
                    "changes": file.changes,
                    "patch": getattr(file, "patch", None),
                    "content": content.decoded_content.decode("utf-8"),
                }
            )

            if len(changed_files) >= MAX_FILES_TO_REVIEW:
                print(f"Reached max files limit ({MAX_FILES_TO_REVIEW})")
                break

        except Exception as e:
            print(f"Error reading file {file.filename}: {e}")
            continue

    return changed_files, pr


def build_review_request(prompt_template: str, changed_files, pr_info) -> str:
    """Build the review request for Gemini"""
    review_request = f"{prompt_template}\n\n---\n\n"
    review_request += "# Pull Request Information\n\n"
    review_request += f"**Title**: {pr_info.title}\n"
    if pr_info.body:
        review_request += f"**Description**: {pr_info.body}\n"
    review_request += f"\n**Files Changed**: {len(changed_files)}\n\n"

    review_request += "# Files to Review\n\n"

    for file_info in changed_files:
        review_request += f"## File: `{file_info['filename']}`\n"
        review_request += f"**Status**: {file_info['status']} "
        review_request += f"(+{file_info['additions']} -{file_info['deletions']})\n\n"

        if file_info["patch"]:
            review_request += "**Changes (diff):**\n```diff\n"
            review_request += file_info["patch"]
            review_request += "\n```\n\n"

        review_request += f"**Full file content:**\n```{fence_language(file_info['filename'])}\n"
        review_request += file_info["content"]
        review_request += "\n```\n\n---\n\n"

    return review_request


def generate_review(client: genai.Client, review_request: str):
    """Call Gemini API to generate code review"""
    try:
        response = client.models.generate_content(
            model=GEMINI_MODEL,
            contents=review_request,
            config=types.GenerateContentConfig(
                temperature=0.7,
                top_p=0.95,
                top_k=40,
                max_output_tokens=8192,
            ),
        )
        return response.text
    except Exception as e:
        print(f"Error generating review: {e}")
        return None


def format_comment(body: str, with_footer: bool = True) -> str:
    """Wrap review text in the marker, heading and footer"""
    comment = f"{REVIEW_COMMENT_MARKER}\n# AI Code Review\n\n{body}"
    if with_footer:
        comment += (
            "\n\n---\n"
            "*This review was automatically generated by AI. "
            "Please use your judgment and feel free to discuss any suggestions!*"
        )
    return comment


def find_existing_review_comment(pr):
    """Find existing AI review comment on the PR"""
    try:
        for comment in pr.get_issue_comments():
            if comment.user.login == BOT_LOGIN and REVIEW_COMMENT_MARKER in comment.body:
                return comment
    except Exception as e:
        print(f"Error finding existing comment: {e}")
    return None


def post_or_update_comment(pr, body: str) -> bool:
    """Post a comment on the PR, or update the existing AI review comment"""
    existing_comment = find_existing_review_comment(pr)
    try:
        if existing_comment:
            existing_comment.edit(body)
            print(f"Updated existing review comment: {existing_comment.html_url}")
        else:
            comment = pr.as_issue().create_comment(body)
            print(f"Posted new review comment: {comment.html_url}")
        return True
    except GithubException as e:
        print(f"Error posting comment: {e}")
        return False


def publish(pr, body: str, dry_run: bool) -> bool:
    """Print the comment in dry-run mode, otherwise post it"""
    if dry_run:
        print("\n" + "=" * 60)
        print("DRY RUN: Would have posted this comment:")
        print("=" * 60 + "\n")
        print(body)
        print("\n" + "=" * 60)
        print("DRY RUN: Review complete (no changes made to GitHub)")
        print("=" * 60)
        return True
    print("Posting review to GitHub...")
    return post_or_update_comment(pr, body)


def main():
    """Main execution function"""
    github_token = os.environ.get("GITHUB_TOKEN")
    gemini_api_key = os.environ.get("GEMINI_API_KEY")
    repo_name = os.environ.get("GITHUB_REPOSITORY")
    pr_number = os.environ.get("PR_NUMBER")
    dry_run = os.environ.get("DRY_RUN", "").lower() in ("true", "1", "yes")

    # A missing key is expected on fork PRs and before the org secret is granted.
    if not gemini_api_key:
        print("::warning::GEMINI_API_KEY is not available; skipping AI code review.")
        sys.exit(0)

    if not all([github_token, repo_name, pr_number]):
        print("Error: Missing required environment variables")
        print(f"GITHUB_TOKEN: {'✓' if github_token else '✗'}")
        print(f"GITHUB_REPOSITORY: {'✓' if repo_name else '✗'}")
        print(f"PR_NUMBER: {'✓' if pr_number else '✗'}")
        sys.exit(1)

    try:
        pr_number = int(pr_number)
    except ValueError:
        print(f"Error: Invalid PR number: {pr_number}")
        sys.exit(1)

    mode_str = "DRY RUN MODE" if dry_run else "LIVE MODE"
    print("=" * 60)
    print(f"Starting AI code review for {repo_name}#{pr_number}")
    print(f"Mode: {mode_str}  Model: {GEMINI_MODEL}")
    print("=" * 60 + "\n")

    print("Initializing Gemini...")
    client = setup_gemini(gemini_api_key)

    print("Connecting to GitHub...")
    repo = Github(auth=Auth.Token(github_token)).get_repo(repo_name)

    print("Loading review prompt template...")
    prompt_template = load_prompt_template()

    print("Fetching changed files...")
    changed_files, pr = get_changed_files(repo, pr_number)

    if not changed_files:
        print("No files to review (or all files were filtered out)")
        no_files_message = format_comment(
            "I didn't find any files that needed review in this PR. "
            "This might be because the changes are to removed files, "
            "or the files are too large to review.",
            with_footer=False,
        )
        if not publish(pr, no_files_message, dry_run):
            sys.exit(1)
        return

    print(f"Found {len(changed_files)} files to review")

    print("Building review request...")
    review_request = build_review_request(prompt_template, changed_files, pr)

    print("Generating AI review with Gemini...")
    review_text = generate_review(client, review_request)

    if not review_text:
        print("Failed to generate review")
        sys.exit(1)

    print("Review generated successfully")

    if publish(pr, format_comment(review_text), dry_run):
        print("Code review completed successfully!")
    else:
        print("Failed to post review")
        sys.exit(1)


if __name__ == "__main__":
    main()
