#!/usr/bin/env bash

set -xe

BASE_PATH=$(cd $(dirname $0) && pwd)
JAVA=$(which java)

if [[ -z "${JAVA}" ]]; then
    echo "Java isn't installed"
    exit 1
fi

pushd ${BASE_PATH} 2>&1 1> /dev/null

MAIN_JAR=$(basename $(find "${BASE_PATH}" -maxdepth 1 -name '*.jar' | tail -1))

${JAVA} -cp "${MAIN_JAR}:lib/*" ru.linachan.yggdrasil.YggdrasilCore

popd 2>&1 1> /dev/null