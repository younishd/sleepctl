#!/usr/bin/env bash

set -o errexit
set -o nounset

PKGNAME='sleepctl'
CONTAINER="${CONTAINER:-bukkit-dev}"
SSHHOST="${SSHHOST:-zion}"

function main() {
    echo "Building..."
    mvn package
    JARFILE=$(basename target/"$PKGNAME"*jar-with-dependencies.jar)
    scp target/"$JARFILE" "$SSHHOST":/tmp/
    ssh "$SSHHOST" -- lxc exec "$CONTAINER" -- bash -c "'"'rm -rvf /home/bukkit/plugins/*'"'"
    ssh "$SSHHOST" -- lxc file -v push /tmp/"$JARFILE" "$CONTAINER"/home/bukkit/plugins/
    ssh "$SSHHOST" -- lxc exec "$CONTAINER" -- bash -c "'"'sudo -u bukkit -- screen -S bukkit -p 0 -X stuff "reload\\015"'"'"
    echo "Done."
}

main "$@"
