#!/bin/bash
LIBPATH=.
source $LIBPATH/config.sh

usage () {
	echo "${_BASENAME}: usage"
	echo ""
	echo "PARAMETERS:"
	echo "  -g=PATH             get path."
	echo "  -s=PATH             set path."
	echo "  -d=PATH             del path."
	echo "  -h                  This message."
	exit 0
}

[[ -z "${1}" ]] && usage

# change to english locale!
export LANG="en_US"

while [ $# -gt 0 ]; do
	case ${1} in
		-g=*|--g=*) getpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-s=*|--s=*) setpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-d=*|--d=*) delpath="$(echo ${1} | awk -F= '{print $2;}')" ;;
		-v|--v) verbose_mode=1 ;;
		-h|--h|?) usage ;;
		*) usage ;;
		esac
	shift
done
# Source the output file ----------------------------------------------------------
if [ "$setpath" != "" ]; then
    read INPUT_DATA
    curl -XPUT -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/$setpath
    if [ "$verbose_mode" = "true" ]; then
     echo >&2 curl -XPUT -d$($JQPath -c -n --arg output "$INPUT_DATA" '{content: $output}') $PROTO://$KVDN_HOST:$KVDN_PORT/$setpath;
    fi
fi
if [ "$getpath" != "" ]; then
    curl -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/$getpath
    if [ "$verbose_mode" = "true" ]; then echo curl -XGET  $PROTO://$KVDN_HOST:$KVDN_PORT/$getpath ; fi

fi
if [ "$delpath" != "" ]; then
    curl -XDELETE  $PROTO://$KVDN_HOST:$KVDN_PORT/$delpath
    if [ "$verbose_mode" = "true" ]; then echo curl -XDELETE  $PROTO://$KVDN_HOST:$KVDN_PORT/$delpath ; fi

fi


# Display arguments
if [ "$verbose_mode" = "true" ]; then
	echo >&2 "Verbose mode ON"
	echo >&2 "setpath  : $setpath"
	echo >&2 "delpath: $delpath"
	echo >&2 "clpth : $clpth"
	echo >&2 "basepath : $PROTO://$KVDN_HOST:$KVDN_PORT/"
fi

# Check if input file exists
if [ "$verbose_mode" = "true" ]; then echo >&2 "Done."; fi

exit 0
