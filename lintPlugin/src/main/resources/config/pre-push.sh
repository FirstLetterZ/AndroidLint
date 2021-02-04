#!/bin/sh

project_path=$(dirname $(dirname $(dirname "$0")))
echo "project_path="$project_path
cd $SCRIPT_PATH

os_type=$(uname -s)
if [[ $os_type =~ "MINGW" || $os_type =~ "Window" ]]; then
  os_type="Window"
elif [[ $os_type =~ "Darwin" ]]; then
  os_type="Mac"
elif [[ $os_type =~ "Linux" ]]; then
  os_type="Linux"
fi
echo "os_type="$os_type

if [[ "Window" == $os_type ]]; then
  cd ${SCRIPT_PATH,0,2}
fi
./gradlew lintForArchon

if [ $? -eq 0 ]; then
  echo "gradle task build successful"
  exit 0
else
  echo "gradle task build failed"
  exit 1
fi

remote="$1"
url="$2"

z40=0000000000000000000000000000000000000000

while read local_ref local_sha remote_ref remote_sha; do
  if [ "$local_sha" = $z40 ]; then
    # Handle delete
    :
  else
    if [ "$remote_sha" = $z40 ]; then
      # New branch, examine all commits
      range="$local_sha"
    else
      # Update to existing branch, examine new commits
      range="$remote_sha..$local_sha"
    fi
    # Check for WIP commit
    commit=$(git rev-list -n 1 --grep '^WIP' "$range")
    if [ -n "$commit" ]; then
      echo >&2 "Found WIP commit in $local_ref, not pushing"
      exit 1
    fi
  fi
done

exit 0