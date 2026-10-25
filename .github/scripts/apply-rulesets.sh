#!/usr/bin/env bash
# Create or update the repository rulesets defined in .github/rulesets/*.json.
# Rulesets are matched by name, so re-running updates them in place.
# Team bypass actors are written as "team:<slug>" and resolved to team ids here.
#
# Usage: .github/scripts/apply-rulesets.sh [--dry-run] [owner/repo]
# Requires: gh (authenticated as a repo admin), jq
set -euo pipefail

DRY_RUN=false
if [[ "${1:-}" == "--dry-run" ]]; then
  DRY_RUN=true
  shift
fi

REPO="${1:-IgniteRobotics/IgniteLib}"
ORG="${REPO%%/*}"
RULESET_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../rulesets" && pwd)"

existing="$(gh api "repos/${REPO}/rulesets" --paginate --jq '.[] | select(.source_type == "Repository") | {id, name}')"

for file in "${RULESET_DIR}"/*.json; do
  payload="$(cat "${file}")"

  for slug in $(jq -r '.bypass_actors[].actor_id | strings | select(startswith("team:")) | ltrimstr("team:")' "${file}"); do
    team_id="$(gh api "orgs/${ORG}/teams/${slug}" --jq '.id')"
    payload="$(jq --arg ref "team:${slug}" --argjson id "${team_id}" \
      '.bypass_actors |= map(if .actor_id == $ref then .actor_id = $id else . end)' <<<"${payload}")"
  done

  name="$(jq -r '.name' <<<"${payload}")"
  id="$(jq -r --arg name "${name}" 'select(.name == $name) | .id' <<<"${existing}" | head -n1)"

  if [[ -n "${id}" ]]; then
    method=PUT
    endpoint="repos/${REPO}/rulesets/${id}"
  else
    method=POST
    endpoint="repos/${REPO}/rulesets"
  fi

  if [[ "${DRY_RUN}" == true ]]; then
    echo "# ${method} ${endpoint}"
    jq . <<<"${payload}"
  else
    gh api -X "${method}" "${endpoint}" --input - <<<"${payload}" --jq '"\(.name) (id \(.id)): \(.enforcement)"'
  fi
done
