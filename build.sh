#!/bin/bash
#
# auto deploy will call this script after `svn up && mv to target`
#

export LANG=en_US.UTF-8

declare -r __PWD__=$(pwd)
declare -r __USER__=$(whoami)
declare -r VERSION=`date "+%Y%m%d%H%M%S"`

echo "${__USER__} build version@${VERSION} ..."
cd $(dirname -- "${0}")

# remove all change files first
# echo "git clean -xfd"
# git checkout ./
# git clean -xfd

# node version
echo "node `node -v`"
echo "npm `npm -v`"

# auto update version file
echo ${VERSION} > configure/version

echo "clean old compile objects and node_modules first..."
make clean && make install
if [ ${?} -ne 0 ] ; then
    echo "node_modules install failed!!!"
    exit 1;
fi
echo "node_modules installed."

envType=$1

declare __ENV__=${envType}

case $envType in
  daily )
  #日常环境
  __ENV__=daily
  ;;
  pre )
  #预发环境
  __ENV__=pre
  ;;
  online )
  __ENV__=online
  ;;
  * )
  #项目环境

  ;;
esac

echo "------------- config env: ${__ENV__} -------------------"
cp configure/config_${__ENV__}.js configure/config.js


# mkdir public dir for sotre status.ckj file
# mkdir ./public
mkdir logs

cd ${__PWD__}

echo "----build success!!!----"

exit 0